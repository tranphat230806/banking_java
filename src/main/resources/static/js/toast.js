document.head.insertAdjacentHTML('beforeend', `
<style>
.toast-container { position: fixed; top: 20px; right: 20px; z-index: 99999; display: flex; flex-direction: column; gap: 10px; } 
.toast { padding: 15px 25px; border-radius: 8px; color: white; font-weight: 500; opacity: 0; transform: translateX(100%); transition: all 0.3s ease; box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; align-items: center; gap: 10px; font-family: sans-serif; font-size: 14px; } 
.toast.show { opacity: 1; transform: translateX(0); } 
.toast-success { background: #10b981; } 
.toast-error { background: #ef4444; } 
.toast-info { background: #3b82f6; }
.confirm-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 100000; display: flex; justify-content: center; align-items: center; opacity: 0; transition: 0.2s; font-family: sans-serif; }
.confirm-overlay.show { opacity: 1; }
.confirm-box { background: white; padding: 25px; border-radius: 12px; min-width: 300px; text-align: center; transform: translateY(-20px); transition: 0.3s; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.confirm-overlay.show .confirm-box { transform: translateY(0); }
.confirm-box p { color: #333; margin-bottom: 20px; font-size: 16px; font-weight: 500; }
.confirm-btn-group { display: flex; gap: 10px; justify-content: center; }
.confirm-btn { padding: 8px 20px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; transition: 0.2s; font-size: 14px; }
.confirm-btn-yes { background: #2563eb; color: white; }
.confirm-btn-yes:hover { background: #1d4ed8; }
.confirm-btn-no { background: #e2e8f0; color: #475569; }
.confirm-btn-no:hover { background: #cbd5e1; }
</style>
`);

function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    let icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showConfirm(message, onConfirm) {
    const overlay = document.createElement('div');
    overlay.className = 'confirm-overlay';
    overlay.innerHTML = `
        <div class="confirm-box">
            <p>${message}</p>
            <div class="confirm-btn-group">
                <button class="confirm-btn confirm-btn-no">Hủy</button>
                <button class="confirm-btn confirm-btn-yes">Xác nhận</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);
    setTimeout(() => overlay.classList.add('show'), 10);

    const close = () => {
        overlay.classList.remove('show');
        setTimeout(() => overlay.remove(), 200);
    };

    overlay.querySelector('.confirm-btn-no').onclick = close;
    overlay.querySelector('.confirm-btn-yes').onclick = () => {
        close();
        if (onConfirm) onConfirm();
    };
}

// Auto check URL params for global notifications
window.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        let msg = urlParams.get('error')
        if (msg === 'true' || msg === '' || msg === null) {
            if (window.location.pathname.includes('login')) msg = 'Tài khoản hoặc mật khẩu không chính xác.';
            else msg = 'Thao tác thất bại.';
        }
        showToast(decodeURIComponent(msg), 'error');
    }
    if (urlParams.has('success')) {
        let msg = urlParams.get('success') || 'Thao tác thành công.';
        if (msg === 'true' || msg === '') msg = 'Thao tác thành công.';
        showToast(decodeURIComponent(msg), 'success');
    }
    if (urlParams.has('logout')) {
        showToast('Đã đăng xuất thành công.', 'success');
    }
});
