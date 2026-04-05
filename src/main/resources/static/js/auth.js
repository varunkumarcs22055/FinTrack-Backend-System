// Setup forms
document.addEventListener('DOMContentLoaded', () => {
    // If logged in, redirect to dashboard
    if (localStorage.getItem('token')) {
        window.location.href = '/dashboard.html';
        return;
    }

    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    
    // Toggle standard UI
    document.getElementById('show-register').addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.add('hidden');
        registerForm.classList.remove('hidden');
    });
    
    document.getElementById('show-login').addEventListener('click', (e) => {
        e.preventDefault();
        registerForm.classList.add('hidden');
        loginForm.classList.remove('hidden');
    });

    // Handle Login
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorEl = document.getElementById('login-error');
        errorEl.textContent = '';
        errorEl.classList.add('hidden');
        
        try {
            const formData = new FormData(loginForm);
            const data = Object.fromEntries(formData);
            
            const response = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            
            const responseData = await response.json();
            
            if (!response.ok) {
                throw new Error(responseData.message || 'Login failed');
            }
            
            // Save token and user info
            localStorage.setItem('token', responseData.token);
            localStorage.setItem('username', responseData.username);
            localStorage.setItem('role', responseData.role);
            
            // Redirect
            window.location.href = '/dashboard.html';
            
        } catch (error) {
            errorEl.textContent = error.message;
            errorEl.classList.remove('hidden');
        }
    });

    // Handle Registration
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorEl = document.getElementById('register-error');
        errorEl.textContent = '';
        errorEl.classList.add('hidden');
        
        try {
            const formData = new FormData(registerForm);
            const data = Object.fromEntries(formData);
            
            // Confirm password
            if (data.password !== data.confirmPassword) {
                throw new Error("Passwords don't match");
            }
            delete data.confirmPassword;
            
            const response = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            
            const responseData = await response.json();
            
            if (!response.ok) {
                const details = responseData.details ? "\n- " + responseData.details.join("\n- ") : "";
                throw new Error(responseData.message + details);
            }
            
            // Success! Save token and redirect
            localStorage.setItem('token', responseData.token);
            localStorage.setItem('username', responseData.username);
            localStorage.setItem('role', responseData.role);
            
            window.location.href = '/dashboard.html';
            
        } catch (error) {
            errorEl.innerText = error.message; // using innerText to keep line breaks if details exist
            errorEl.classList.remove('hidden');
        }
    });

    // Handle credential auto-fill for testing/demo
    document.querySelectorAll('.demo-login').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            document.getElementById('loginUsername').value = e.target.dataset.username;
            document.getElementById('loginPassword').value = 'admin123'.replace('admin', e.target.dataset.username.replace('1', '')); // basic fallback if not 'admin'
            if (e.target.dataset.username === 'admin') document.getElementById('loginPassword').value = 'admin123';
            if (e.target.dataset.username === 'analyst') document.getElementById('loginPassword').value = 'analyst123';
            if (e.target.dataset.username === 'viewer') document.getElementById('loginPassword').value = 'viewer123';
            loginForm.dispatchEvent(new Event('submit', { cancelable: true }));
        });
    });
});
