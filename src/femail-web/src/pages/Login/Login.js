import { useState } from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const [errors, setErrors] = useState({});
  const [loginError, setLoginError] = useState('');

  const validate = () => {
    const newErrors = {};

    if (!formData.username) {
      newErrors.username = 'Username is required.';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
     if (loginError) {
      setLoginError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      const response = await fetch('http://localhost:8080/api/tokens', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        setLoginError('Login failed. Please check your credentials.');
        return;
      }

      const data = await response.json();
      localStorage.setItem('token', data.token); // Store the token in localStorage
      const payload = JSON.parse(atob(data.token.split('.')[1]));
      localStorage.setItem('userId', payload.user_id);
      navigate('/mails');  // Redirect to the mails page after successful login

    } catch (err) {
      console.error('Login request failed:', err);
      setLoginError('An error occurred. Please try again later.');
    }
  };

  return (
  <div className="login-container">
    <form id="loginForm" onSubmit={handleSubmit}>
      <div className="form-header">
        <img src="/favicon.ico" alt="FeMail Logo" />
        <h2>Login to FeMail.com</h2>
      </div>

      {loginError && (
        <div className="login-error-message">
          {loginError}
        </div>
      )}

      <div className="fields-wrapper">
        <div>
          <label htmlFor="username">Username <span className="required">*</span></label>
          <div className="login-error-message">{errors.username}</div>
          <div className="input-wrapper">
            <input
              type="text"
              name="username"
              id="username"
              value={formData.username}
              onChange={handleChange}
              required
              className={(errors.username || loginError) ? 'login-input-error' : ''}
            />
            <span className="input-suffix">@femail.com</span>
          </div>
        </div>

        <div>
          <label htmlFor="password">Password <span className="required">*</span></label>
          <div className="field-error">{errors.password}</div>
          <input
            type="password"
            name="password"
            id="password"
            value={formData.password}
            onChange={handleChange}
            required
            className={(errors.password || loginError) ? 'login-input-error' : ''}
          />
        </div>
      </div>

      <button className='button-login' type="submit">Login</button>
      <div className="login-footer">
        <span>Don't have an account?</span>
        <button type="button" className="link-button" onClick={() => navigate('/register')}>
          Register here
        </button>
      </div>
    </form>
  </div>
);
}
