import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { AiOutlineDelete } from "react-icons/ai";
import { useNavigate, useOutletContext } from "react-router-dom";
import { useMails } from "../../hooks/useMails";

const TrashMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { trashMails, setTrashMails } = useOutletContext();
  const [selectedMails, setSelectedMails] = useState([]);
  const [error, ] = useState(null);
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
        const filtered = data.filter((mail) => mail.isDeleted === true);
        setTrashMails(filtered);
      })
      .catch((err) => {
        console.error(err);
        
      });
  }, [token, userId, setTrashMails]);

  const handleOpenMail = (e, mailId) => {
    const ignore = ["BUTTON", "SVG", "PATH", "INPUT"];
    if (ignore.includes(e.target.tagName)) return;
    navigate(`/mails/trash/${mailId}`);
  };


  const { permanentDeleteSelectedMails, markSpam } = useMails(token, userId, trashMails, setTrashMails);
  
    const handlePermanentDeleteSelected = () => {
    permanentDeleteSelectedMails(selectedMails, setTrashMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, trashMails, setTrashMails, setSelectedMails);
  };

  const handleMoveToLabel = (labelId) => {
    if (labelId === "Spam") {
      handleMarkSpam();
      return;
    }

    selectedMails.forEach((id) => {
      const body = {
        category: labelId === "Inbox" ? "Primary" : labelId,
        isSpam: false,
        isDeleted: false
      };

      if (labelId === "Inbox") {
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

    setTrashMails((prev) =>
      prev.filter((mail) => !selectedMails.includes(mail.id))
    );
    setSelectedMails([]);
  };


  const handleCheckboxChange = (e, id) => {
    e.stopPropagation();
    if (e.target.checked) {
      setSelectedMails((prev) => [...prev, id]);
    } else {
      setSelectedMails((prev) => prev.filter((mid) => mid !== id));
    }
  };

  return (
    <div className="trash-container">
      
      <MailActionBar
        selected={selectedMails}
        onDelete={handlePermanentDeleteSelected}
        onMarkSpam={handleMarkSpam}
        onClear={() => setSelectedMails([])}
        onMoveTo={handleMoveToLabel}
      />

      {error && <p className="error">{error}</p>}
      {trashMails.length === 0 && !error && <p>No deleted messages.</p>}

      <ul className="mail-list">
        {trashMails.map((mail) => (
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

            <span className="mail-from">{mail.from}</span>

            <div className="mail-subject">
              <span className="subject-text">{mail.subject}</span>
              <span className="mail-body-preview"> - {mail.body.slice(0, 40)}...</span>
            </div>

            <span className="mail-date">
              {new Date(mail.timestamp).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric"
              })}
            </span>

            <span
              className="mail-delete"
              onClick={(e) => {e.stopPropagation(); handlePermanentDeleteSelected();}}
              title="delete"
            >
              <AiOutlineDelete />
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TrashMail;
