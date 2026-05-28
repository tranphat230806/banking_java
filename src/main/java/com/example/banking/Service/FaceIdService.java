package com.example.banking.Service;

import com.example.banking.Entity.AdminClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.AdminRepository;
import com.example.banking.Repository.UserRepository;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class FaceIdService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    private CascadeClassifier faceDetector;
    private final String UPLOAD_DIR = "uploads/faces/";

    @PostConstruct
    public void init() {
        OpenCV.loadShared();
        String cascadePath = Paths.get("src/main/resources/haarcascade_frontalface_default.xml").toAbsolutePath().toString();
        faceDetector = new CascadeClassifier(cascadePath);

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ---- UserClass overloads ----

    public boolean registerFace(UserClass user, String base64Image) {
        Mat face = extractFace(base64Image);
        if (face == null || face.empty()) return false;

        String filename = UPLOAD_DIR + "user_" + user.getId() + "_face.jpg";
        Imgcodecs.imwrite(filename, face);

        user.setFaceRegistered(true);
        user.setFaceImagePath(filename);
        userRepository.save(user);
        return true;
    }

    public boolean verifyFace(UserClass user, String base64Image) {
        if (!user.isFaceRegistered() || user.getFaceImagePath() == null) return false;
        return compareFace(base64Image, user.getFaceImagePath(), user.getUsername());
    }

    // ---- AdminClass overloads ----

    public boolean registerFace(AdminClass admin, String base64Image) {
        Mat face = extractFace(base64Image);
        if (face == null || face.empty()) return false;

        String filename = UPLOAD_DIR + "admin_" + admin.getId() + "_face.jpg";
        Imgcodecs.imwrite(filename, face);

        admin.setFaceRegistered(true);
        admin.setFaceImagePath(filename);
        adminRepository.save(admin);
        return true;
    }

    public boolean verifyFace(AdminClass admin, String base64Image) {
        if (!admin.isFaceRegistered() || admin.getFaceImagePath() == null) return false;
        return compareFace(base64Image, admin.getFaceImagePath(), admin.getUsername());
    }

    // ---- Private helpers ----

    private boolean compareFace(String base64Image, String storedImagePath, String username) {
        Mat liveFace = extractFace(base64Image);
        if (liveFace == null || liveFace.empty()) return false;

        Mat storedFace = Imgcodecs.imread(storedImagePath, Imgcodecs.IMREAD_GRAYSCALE);
        if (storedFace.empty()) return false;

        Size stdSize = new Size(200, 200);
        Mat liveResized = new Mat();
        Mat storedResized = new Mat();
        Imgproc.resize(liveFace, liveResized, stdSize);
        Imgproc.resize(storedFace, storedResized, stdSize);

        Mat result = new Mat();
        Imgproc.matchTemplate(storedResized, liveResized, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        double similarity = mmr.maxVal;
        System.out.println(">>> FaceID Verify - User: " + username + " - Similarity score: " + similarity);

        return similarity > 0.55;
    }

    private Mat extractFace(String base64Image) {
        try {
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            MatOfByte matOfByte = new MatOfByte(imageBytes);
            Mat img = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

            if (img.empty()) return null;

            Mat gray = new Mat();
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray, gray);

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(gray, faceDetections, 1.1, 3, 0, new Size(100, 100), new Size());

            Rect[] rects = faceDetections.toArray();
            if (rects.length == 0) return null;

            Rect targetFace = rects[0];
            for (Rect rect : rects) {
                if (rect.area() > targetFace.area()) {
                    targetFace = rect;
                }
            }

            return new Mat(gray, targetFace);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
