package com.example.banking.Service;

import com.example.banking.Entity.UserClass;
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
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class FaceIdService {

    @Autowired
    private UserRepository userRepository;

    private CascadeClassifier faceDetector;
    private final String UPLOAD_DIR = "uploads/faces/";

    @PostConstruct
    public void init() {
        OpenCV.loadShared();
        // Load cascade classifier from resources
        String cascadePath = Paths.get("src/main/resources/haarcascade_frontalface_default.xml").toAbsolutePath().toString();
        faceDetector = new CascadeClassifier(cascadePath);
        
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public boolean registerFace(UserClass user, String base64Image) {
        Mat face = extractFace(base64Image);
        if (face == null || face.empty()) return false;

        String filename = UPLOAD_DIR + user.getId() + "_face.jpg";
        Imgcodecs.imwrite(filename, face);

        user.setFaceRegistered(true);
        user.setFaceImagePath(filename);
        userRepository.save(user);

        return true;
    }

    public boolean verifyFace(UserClass user, String base64Image) {
        if (!user.isFaceRegistered() || user.getFaceImagePath() == null) return false;

        Mat liveFace = extractFace(base64Image);
        if (liveFace == null || liveFace.empty()) return false;

        Mat storedFace = Imgcodecs.imread(user.getFaceImagePath(), Imgcodecs.IMREAD_GRAYSCALE);
        if (storedFace.empty()) return false;

        // Resize both to standard size for comparison
        Size stdSize = new Size(200, 200);
        Mat liveResized = new Mat();
        Mat storedResized = new Mat();
        Imgproc.resize(liveFace, liveResized, stdSize);
        Imgproc.resize(storedFace, storedResized, stdSize);

        // Simple Mean Squared Error (MSE) comparison or SSIM
        // For simplicity and speed, we use Template Matching CCOEFF_NORMED
        Mat result = new Mat();
        Imgproc.matchTemplate(storedResized, liveResized, result, Imgproc.TM_CCOEFF_NORMED);
        
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        double similarity = mmr.maxVal; // 1.0 is a perfect match
        System.out.println(">>> FaceID Verify - User: " + user.getUsername() + " - Similarity score: " + similarity);

        // Threshold for face match (adjustable)
        return similarity > 0.55; // Slightly lower threshold for better UX with template matching
    }

    private Mat extractFace(String base64Image) {
        try {
            // Remove data:image/jpeg;base64, prefix if present
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
            if (rects.length == 0) return null; // No face detected

            // Assume the largest face is the target
            Rect targetFace = rects[0];
            for (Rect rect : rects) {
                if (rect.area() > targetFace.area()) {
                    targetFace = rect;
                }
            }

            // Crop face
            Mat croppedFace = new Mat(gray, targetFace);
            return croppedFace;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
