import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";


const SendMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { sendMails, setSendMails } = useOutletContext();
  const [error, setError] = useState(null);
  const [selectedMails, setSelectedMails] = useState([]);
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
          (mail) => mail.direction.includes("sent") && mail.isSpam !== true && mail.isDeleted === false
        );
        setSendMails(filtered);
      })
      .catch((err) => {
        console.error(err);
        
      });
  }, []);


  const { deleteSelectedMails, markSpam, handleStarredMail, handleDeleteMail } = useMails(token, userId, sendMails, setSendMails);
  
    const handleDeleteSelected = () => {
    deleteSelectedMails(selectedMails, setSendMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, sendMails, setSendMails, setSelectedMails);
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
    navigate(`/mails/sent/${mailId}`);
  };

  return (
    <div className="send-container">
      <MailActionBar
        selected={selectedMails}
        onDelete={handleDeleteSelected}
        onMarkSpam={handleMarkSpam}
        onClear={() => setSelectedMails([])}
      />

      {error && <p className="error">{error}</p>}
      {sendMails.length === 0 && !error && <p>No messages.</p>}

      <ul className="mail-list">
        {sendMails.map((mail) => (
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
              onClick={(e) => handleStarredMail(e, mail, setSendMails)}
            />

            <span className="mail-from">To: {mail.to.join(", ")}</span>

            <div className="mail-subject">
              <span className="subject-text">{mail.subject}</span>
              <span className="mail-body-preview">
                {" "}
                - {mail.body.slice(0, 40)}...
              </span>
            </div>

            <span className="mail-date">
              {new Date(mail.timestamp).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric"
              })}
            </span>

            <span
              className="mail-delete"
              onClick={(e) => handleDeleteMail(e, mail.id, setSendMails)}
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

export default SendMail;
