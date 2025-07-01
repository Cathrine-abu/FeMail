import { useState, useRef } from 'react';
import './Register.css';
import { useNavigate } from 'react-router-dom';

export default function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    full_name: '',
    phone: '',
    birth_date: '',
    gender: '',
    image: null,
  });

  const [errors, setErrors] = useState({});
  const [previewUrl, setPreviewUrl] = useState(null);
  const imageInputRef = useRef();

  const validate = () => {
    const newErrors = {};

    if (!formData.username || !/^[a-z0-9]+$/.test(formData.username) || (formData.username.match(/[a-z]/g) || []).length < 3) {
      newErrors.username = 'Username must contain only lowercase letters and numbers, and include at least 3 letters.';
    }
    if (!formData.password || formData.password.length < 8 || !/\d/.test(formData.password) || !/[A-Za-z]/.test(formData.password)) {
      newErrors.password = 'Password must be at least 8 characters and include letters and numbers.';
    }
    if (formData.confirmPassword !== formData.password) {
      newErrors.confirmPassword = 'Passwords do not match.';
    }
    if (!formData.full_name) {
      newErrors.full_name = 'Full name is required.';
    } else if (!/^[a-zA-Z\s]+$/.test(formData.full_name)) {
      newErrors.full_name = 'Full name can only contain English letters and spaces.';
    }
    if (!/^\d{10}$/.test(formData.phone)) {
      newErrors.phone = 'Phone must be 10 digits.';
    }
    if (!formData.birth_date) {
      newErrors.birth_date = 'Birth date is required.';
    } else {
      const birth = new Date(formData.birth_date);
      const today = new Date();

      const age = today.getFullYear() - birth.getFullYear();
      const monthDiff = today.getMonth() - birth.getMonth();
      const dayDiff = today.getDate() - birth.getDate();

      const is14OrOlder =
        age > 14 ||
        (age === 14 && (monthDiff > 0 || (monthDiff === 0 && dayDiff >= 0)));

      if (!is14OrOlder) {
        newErrors.birth_date = 'You must be at least 14 years old.';
      }
    }
    if (!formData.gender) newErrors.gender = 'Gender is required.';
    if (!formData.image) newErrors.image = 'Profile picture is required.';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value, type, files } = e.target;

    if (type === 'file') {
      if (files && files[0]) {
        const file = files[0];
        
        // Check if file is actually an image
        if (!file.type.startsWith('image/')) {
          setErrors((prev) => ({ ...prev, image: 'Please select a valid image file.' }));
          e.target.value = '';
          return;
        }
        
        // Check file size (max 5MB)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
          setErrors((prev) => ({ ...prev, image: 'File size must be less than 5MB.' }));
          e.target.value = '';
          return;
        }

        // Create a canvas to resize the image
        const reader = new FileReader();
        reader.onload = (event) => {
          const img = new Image();
          img.onload = () => {
            // Create canvas
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');

            // Set dimensions (maintain aspect ratio)
            const size = Math.min(img.width, img.height);
            canvas.width = size;
            canvas.height = size;

            // Draw image centered and cropped to a square
            ctx.drawImage(
              img,
              (img.width - size) / 2,
              (img.height - size) / 2,
              size,
              size,
              0,
              0,
              size,
              size
            );

            // Convert to base64 with good quality
            const base64String = canvas.toDataURL('image/jpeg', 0.9);
            setFormData((prev) => ({ ...prev, image: base64String }));
            setPreviewUrl(base64String);
          };
          img.src = event.target.result;
        };
        reader.readAsDataURL(file);
        
        // Clear any previous errors
        setErrors((prev) => ({ ...prev, image: '' }));
      } else {
        setFormData((prev) => ({ ...prev, image: null }));
        setPreviewUrl(null);
      }
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const isValid = validate();
    if (!isValid) return;

    try {
      const response = await fetch("http://localhost:8080/api/users", {
        method: "POST",
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password,
          full_name: formData.full_name,
          phone: formData.phone,
          birth_date: formData.birth_date,
          gender: formData.gender,
          image: formData.image
        })
      });

      const data = response.status !== 204 ? await response.json() : {};

      if (!response.ok) {
        if (data.error === "Username already exists") {
          setErrors((prevErrors) => ({
            ...prevErrors,
            username: data.error
          }));
        } else {
          alert("Registration failed: " + (data.error || response.status));
        }
        return;
      }

      navigate('/');

    } catch (err) {
      alert("An error occurred. Please try again later.");
    }
  };

  return (
    <div className="register-container">
      <form id="registerForm" onSubmit={handleSubmit}>
        <div className="form-header">
          <img src="/favicon.ico" alt="FeMail Logo" />
          <h2>Register to FeMail.com</h2>
        </div>

        <div>
          <label htmlFor="username">Username <span className="required">*</span></label>
          <div className="field-error">{errors.username}</div>
          <div className="input-wrapper">
            <input
              type="text"
              name="username"
              id="username"
              value={formData.username}
              onChange={handleChange}
              required
              className={errors.username ? 'input-error' : ''}
            />
            <span className="input-suffix">@femail.com</span>
          </div>
        </div>

        <div>
          <label htmlFor="password">Password <span className="required">*</span></label>
          {errors.password && (
            <div className="field-error">{errors.password}</div>  
          )}
          <input
            type="password"
            name="password"
            id="password"
            value={formData.password}
            onChange={handleChange}
            required
            className={errors.password ? 'input-error' : ''}
          />
        </div>

        <div>
          <label htmlFor="confirmPassword">Confirm Password <span className="required">*</span></label>
          {errors.confirmPassword && (
            <div className="field-error">{errors.confirmPassword}</div>
          )}
          <input
            type="password"
            name="confirmPassword"
            id="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            className={errors.confirmPassword ? 'input-error' : ''}
          />
        </div>

        <div>
          <label htmlFor="full_name">Full Name <span className="required">*</span></label>
          <div className="field-error">{errors.full_name}</div>
          <input
            type="text"
            name="full_name"
            id="full_name"
            value={formData.full_name}
            onChange={handleChange}
            required
            className={errors.full_name ? 'input-error' : ''}
          />
        </div>

        <div>
          <label htmlFor="phone">Phone Number <span className="required">*</span></label>
          <div className="field-error">{errors.phone}</div>
          <input
            type="text"
            name="phone"
            id="phone"
            value={formData.phone}
            onChange={handleChange}
            required
            className={errors.phone ? 'input-error' : ''}
          />
        </div>

        <div>
          <label htmlFor="birth_date">Birth Date <span className="required">*</span></label>
          <div className="field-error">{errors.birth_date}</div>
          <input
            type="date"
            name="birth_date"
            id="birth_date"
            value={formData.birth_date}
            onChange={handleChange}
            required
            className={errors.birth_date ? 'input-error' : ''}
          />
        </div>

        <div>
          <label htmlFor="gender">Gender <span className="required">*</span></label>
          <div className="field-error">{errors.gender}</div>
          <select
            name="gender"
            id="gender"
            value={formData.gender}
            onChange={handleChange}
            required
            className={errors.gender ? 'input-error' : ''}
          >
            <option value="">Select gender</option>
            <option value="female">Female</option>
            <option value="male">Male</option>
            <option value="other">Other</option>
          </select>
        </div>

        <div className="profile-pic-container">
          <label htmlFor="image">Upload Profile Picture <span className="required">*</span></label>
          <div className="field-error">{errors.image}</div>
          <label htmlFor="image" className="profile-pic-label">
            Choose Image
          </label>
          <input
            type="file"
            name="image"
            id="image"
            accept="image/*"
            onChange={handleChange}
            ref={imageInputRef}
            className={errors.image ? 'input-error' : ''}
          />
        </div>

        <div id="preview">
          {previewUrl && <img src={previewUrl} alt="Profile Preview" />}
        </div>

        <button className='button-register' type="submit">Submit</button>
      </form>
    </div>
  );
}
