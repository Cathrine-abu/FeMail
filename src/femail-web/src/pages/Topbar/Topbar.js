import { useState, useEffect } from "react";
import "./Topbar.css";
import { AiOutlineSearch, AiOutlineClose } from "react-icons/ai";
import Profile from "../../popUps/Profile/Profile";
import { useGetUser } from "../../hooks/useUsers";
import '../../components/Button/CircleButton.css';
import DarkModeToggle from "../../components/DarkMode/DarkModeToggle";
import { useNavigate } from 'react-router-dom';


const Topbar = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const navigate = useNavigate();
  const [showProfile, setProfile] = useState(false);
  const [searchValue, setSearchValue] = useState("");
  const [darkMode, setDarkMode] = useState(false);
  const { user, } = useGetUser(token, userId);

  const handleKeyDown = (e) => {
      if (e.key === "Enter" && searchValue.trim()) {
          e.preventDefault();
          handleSearch()
      }
  };

  const handleSearch = () => {
    if (searchValue.trim()) {
      navigate(`/mails/search/${encodeURIComponent(searchValue)}`);
    }
  };

  useEffect(() => {
    if (darkMode) {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }
  }, [darkMode]);

  return (
    <div className="topbar">
      <div className="topbar-head"><img src="/favicon.ico" alt="FeMail Logo" className="femail-pic"/>Femail</div>

      <div className="topbar-search">
        <span className="circle-button"
          onClick={() => handleSearch()}>
          <AiOutlineSearch />
        </span>
        <input
          type="text"
          placeholder="Search"
          className="search-input"
          value={searchValue}
          onKeyDown={handleKeyDown}
          onChange={(e) => setSearchValue(e.target.value)}
        />
        <span className="circle-button"
          onClick={() => setSearchValue("")}>
          <AiOutlineClose />
        </span>
      </div>

      <DarkModeToggle isDark={darkMode} onChange={() => setDarkMode(!darkMode)}/>
      <img src={user?.image || "/favicon.ico"} className="profile-pic" onClick={() => setProfile(true)} alt="profile"/>
      <Profile show={showProfile} onClose={() => setProfile(false)} user={user} />
    </div>
  );
};

export default Topbar;
