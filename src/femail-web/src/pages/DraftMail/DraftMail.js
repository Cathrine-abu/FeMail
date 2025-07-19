import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";
import EditMail from "../../popUps/EditMail/EditMail";

const DraftsMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const [selectedMails, setSelectedMails] = useState([]);
  const [error, ] = useState(null);
  const navigate = useNavigate();
  const [showCompose, setShowCompose] = useState(false);
  const [selectedDraft, setSelectedDraft] = useState(null);
  const { draftMails, setDraftMails, addMail, addDraftMail, updateDraftMail } = useOutletContext();


  // No need to fetch data here - parent component handles it

  const { deleteSelectedMails, handleStarredMail, handleDeleteMail } = useMails(token, userId, draftMails, setDraftMails);

  const handleCheckboxChange = (e, id) => {
    e.stopPropagation();
    if (e.target.checked) {
      setSelectedMails((prev) => [...prev, id]);
    } else {
      setSelectedMails((prev) => prev.filter((mid) => mid !== id));
    }
  };

  const handleOpenDraft = (e, mail) => {
    const ignore = ["BUTTON", "SVG", "PATH", "INPUT"];
    if (ignore.includes(e.target.tagName)) return;

    if (mail.isDraft) {
      setSelectedDraft(mail);     
      setShowCompose(true);    
    } else {
      navigate(`/mails/${mail.id}`); 
    }
  };


  const handleMoveToInbox = () => {
    selectedMails.forEach((id) => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId,
        },
        body: JSON.stringify({ direction: ["draft", "received"], isDraft: true, category: "Primary" }),
      }).catch((err) => console.error("Move to inbox failed:", err));
    });
    setSelectedMails([]);
  };

  return (
    <div className="inbox-container">
      <MailActionBar
        selected={selectedMails}
        onDelete={() => deleteSelectedMails(selectedMails, setDraftMails, setSelectedMails)}
        onClear={() => setSelectedMails([])}
        onMoveTo={handleMoveToInbox} 
      />

      {error && <p className="error">{error}</p>}
      {draftMails.length === 0 && !error && <p>No drafts available.</p>}

      <ul className="mail-list">
        {draftMails.map((mail) => (
          
          <li
            key={mail.id}
            className="mail-item"
            onClick={(e) => handleOpenDraft(e, mail)}
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
              onClick={(e) => handleStarredMail(e, mail, setDraftMails)}
            />

            <span className="mail-label">Draft</span>

            <span className="subject-text">{mail.subject || "(no subject)"}
              <span className="mail-body-preview"> - {mail.body.slice(0, 40)}...</span>
            </span>

              <div className="mail-right">
                <span className="mail-date">
                  {new Date(mail.timestamp).toLocaleDateString("en-US", {
                    month: "short",
                    day: "numeric",
                  })}
                </span>

                <span
                  className="mail-delete"
                  onClick={(e) => handleDeleteMail(e, mail.id, setDraftMails)}
                  title="Delete"
                >
                  <AiOutlineDelete />
                </span>
              </div>
          </li>
        ))}
      </ul>
      {showCompose && (
        <EditMail
          show={showCompose}
          onClose={() => {
            setShowCompose(false);
            setSelectedDraft(null);
          }}
          addMail={addMail}
          addDraftMail={addDraftMail}
          updateDraftMail={updateDraftMail}
          draftMail={selectedDraft}
        />
      )}

    </div>
  );
  
};

export default DraftsMail;
