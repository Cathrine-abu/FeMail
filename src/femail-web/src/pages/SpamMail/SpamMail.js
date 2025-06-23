import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";


const SpamMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { spamMails, setSpamMails } = useOutletContext();
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
          (mail) => mail.isSpam === true && mail.isDeleted === false && mail.isDraft === false && !mail.direction.includes("send")
        );
        setSpamMails(filtered);
      })
      .catch((err) => {
        console.error(err);
        
      });
  }, [token, userId, setSpamMails]);

  const { deleteSelectedMails, unMarkSpam, handleStarredMail, handleDeleteMail } = useMails(token, userId, spamMails, setSpamMails);
    
      const handleDeleteSelected = () => {
      deleteSelectedMails(selectedMails, setSpamMails, setSelectedMails);
    };
  
    const handleUnmarkSpam = () => {
      unMarkSpam(selectedMails, spamMails, setSpamMails, setSelectedMails);
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
    navigate(`/mails/spam/${mailId}`);
  };

const handleMoveToLabel = (labelId) => {
  selectedMails.forEach((id) => {
    const body = {
      category: labelId,
      isSpam: false
    };

    if (labelId !== "trash") {
      body.direction = ["received"];
    }
    fetch(`http://localhost:8080/api/mails/${id}`, {
      method: "PATCH",
      headers: {
        "Authorization": `Bearer ${token}`,
        "user-id": userId,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(body)
    }).catch(err => console.error("Failed to move mail:", err));
  });

    setSpamMails((prev) => prev.filter((mail) => !selectedMails.includes(mail.id)));
    setSelectedMails([]);
  };

  return (
    <div className="inbox-container">

      <MailActionBar
        selected={selectedMails}
        onDelete={handleDeleteSelected}
        onMarkSpam={handleUnmarkSpam}
        onClear={() => setSelectedMails([])}
        onMoveTo={handleMoveToLabel}
        markSpamLabel="Remove from Spam"
      />

      {error && <p className="error">{error}</p>}
      {spamMails.length === 0 && !error && <p>No spam messages.</p>}

      <ul className="mail-list">
        {spamMails.map((mail) => (
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
              onClick={(e) => handleStarredMail(e, mail, setSpamMails)}
            />

            <span className="mail-from">{mail.from}</span>

            <span className="mail-subject">
              <span className="subject-text">{mail.subject}</span>
              <span className="mail-body-preview">
                {" "}
                - {mail.body.slice(0, 40)}...
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
              onClick={(e) => handleDeleteMail(e, mail.id, setSpamMails)}
              title="Permanently delete"
            >
              <AiOutlineDelete />
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default SpamMail;
