import "./Profile.css";
import '../../components/PopUp/PopUp.css';
import '../../components/Button/CircleButton.css';
import Button from '../../components/Button/Button';
import { AiOutlineClose, AiOutlineMan, AiOutlineWoman } from "react-icons/ai";
import { useNavigate } from 'react-router-dom';

const Profile = ({ show, onClose, user }) => {
    const navigate = useNavigate();
    if (!show) return null;

    return (
        <div className="pop-up-overlay profile-overlay">
            <div className="pop-up-box profile-box">
                <span className="circle-button profile-header-icon" onClick={onClose}><AiOutlineClose /></span>
                <div className="profile-header">
                    <span className="profile-header-text">{user.username + "@femail.com"}</span>
                </div>
                <img src={user?.image || "/favicon.ico"} className="popup-pic" alt="Profile" />
                <div className="popup-info">
                    <div className="profile-hi">Hi, {user.full_name}! {user.gender === "female" ? <AiOutlineWoman  /> : user.gender === "male" ? <AiOutlineMan  /> : null}</div>
                    <div className="profile-content">Birthday: {user.birth_date}</div>
                    <div className="profile-content">Phone number: {user.phone}</div>
                    <hr />
                    <div className="profile-button-raw">
                        <Button variant = 'grey'
                            onClick={() => {localStorage.removeItem('token'); navigate('/');}}>
                            Log out
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;
