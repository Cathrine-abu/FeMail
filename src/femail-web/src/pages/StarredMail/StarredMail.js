import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";


const StarredMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const [starredMails, setStarredMails] = useState([]);
  const [selectedMails, setSelectedMails] = useState([]);
  const [error,] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetch("http://localhost:8080/api/mails", {
      headers: {
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch mails");
        return res.json();
      })
      .then((data) => {
        const filtered = data.filter(
          (mail) =>
            mail.isStarred === true &&
            (mail.isDeleted === false || mail.isDeleted === undefined) 

        );
        setStarredMails(filtered);
      })
      .catch((err) => {
        console.error(err);
        
      });
  }, [token, userId]);

  const { deleteSelectedMails, markSpam, toggleStar, handleDeleteMail } = useMails(token, userId, starredMails, setStarredMails);
  
    const handleDeleteSelected = () => {
    deleteSelectedMails(selectedMails, setStarredMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, starredMails, setStarredMails, setSelectedMails);
  };


  const handleCheckboxChange = (e, id) => {
    e.stopPropagation();
    if (e.target.checked) {
      setSelectedMails((prev) => [...prev, id]);
    } else {
      setSelectedMails((prev) => prev.filter((mid) => mid !== id));
    }
  };

  const handleOpenMail = (e, mailId) => {
    const ignore = ["BUTTON", "SVG", "PATH", "INPUT"];
    if (ignore.includes(e.target.tagName)) return;
    navigate(`/mails/starred/${mailId}`);
  };

  return (
    <div className="starred-container">
      <MailActionBar
        selected={selectedMails}
        onDelete={handleDeleteSelected}
        onMarkSpam={handleMarkSpam}
        onClear={() => setSelectedMails([])}
      />

      {error && <p className="error">{error}</p>}
      {starredMails.length === 0 && !error && <p>No starred messages.</p>}

      <ul className="mail-list">
        {starredMails.map((mail) => (
          <li
            key={mail.id}
            className="mail-item"
            onClick={(e) => handleOpenMail(e, mail.id)}
          >
            <input
              type="checkbox"
              className="mail-checkbox"
              checked={selectedMails.includes(mail.id)}
              onChange={(e) => handleCheckboxChange(e, mail.id)}
              title="select"

            />

            <MailStarButton
              isStarred={mail.isStarred}
              onClick={(e) => toggleStar(token, e, mail, setStarredMails)}
            />

            <span className="mail-from">{mail.from}</span>

            <span className="mail-subject">
              <span className="subject-text">{mail.subject}</span>
              <span className="mail-body-preview">
                {" "}- {mail.body.slice(0, 40)}...
              </span>
            </span>

            <span className="mail-date">
              {new Date(mail.timestamp).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric"
              })}
            </span>

            <span
              className="mail-delete"
              onClick={(e) => handleDeleteMail(e, mail.id, setStarredMails)}
              title="Delete"
            >
              <AiOutlineDelete />
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default StarredMail;
