import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import MailListTab from "./MailListTab";
import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AiOutlineDelete, AiOutlineStop, AiOutlineClose } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";
import EditMail from "../../popUps/EditMail/EditMail";
import Topbar from "../Topbar/Topbar";


const InboxMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { inboxMails, setInboxMails, addMail, updateDraftMail } = useOutletContext();
  const [error, ] = useState(null);
  const [selectedTab, setSelectedTab] = useState("Primary");
  const [selectedMails, setSelectedMails] = useState([]);
  const navigate = useNavigate();
  const [showDraftEdit, setShowDraftEdit] = useState(false);
  const [selectedDraft, setSelectedDraft] = useState(null);

  // Filter inbox mails based on selected tab
  const filteredInboxMails = inboxMails.filter(mail => 
    (mail.category ?? "Primary") === selectedTab
  );


  const { deleteSelectedMails, markSpam, handleStarredMail, handleDeleteMail } = useMails(token, userId, inboxMails, setInboxMails);

  const handleDeleteSelected = () => {
    deleteSelectedMails(selectedMails, setInboxMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, inboxMails, setInboxMails, setSelectedMails);
  };

  const handleMoveToLabel = (labelId) => {
    if (labelId === "Spam") {
      handleMarkSpam();
      return;
    }

    if (labelId === "Trash") {
      handleDeleteSelected();
      return;
    }

    selectedMails.forEach((id) => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: {
          "Authorization": `Bearer ${token}`,
          "user-id": userId,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ category: labelId }),
      });
    });
    if (selectedTab !== labelId) {
      setInboxMails((prev) => prev.filter((mail) => !selectedMails.includes(mail.id)));
    }
    setSelectedMails([]);
  }; 

  const handleOpenMail = (e, mail) => {

    const ignore = ["BUTTON", "SVG", "PATH", "INPUT"];
    if (ignore.includes(e.target.tagName)) return;

    if (mail.direction.includes("draft")) {
        setSelectedDraft(mail);
        setShowDraftEdit(true);
    } else {
      fetch(`http://localhost:8080/api/mails/${mail.id}`, {
        method: "PATCH",
        headers: {
          "Authorization": `Bearer ${token}`,
          "user-id": userId,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ isRead: true })
      });

      navigate(`/mails/inbox/${mail.id}`);
    }
  };

  const handleMoveToTrash = (mailIds) => {
    mailIds.forEach(id => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: { "user-id": userId, "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify({ isDeleted: true }),
      });
    });
    setInboxMails(prev => prev.filter(mail => !mailIds.includes(mail.id)));
    setSelectedMails([]);
  };

  const handleMoveToSpam = (mailIds) => {
    mailIds.forEach(id => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: { "user-id": userId, "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify({ isSpam: true, direction: ["spam"] }),
      });
    });
    setInboxMails(prev => prev.filter(mail => !mailIds.includes(mail.id)));
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
    <div className="inbox-container">
      {selectedMails.length > 0 && (
        <div className="contextual-action-bar" style={{
          display: "flex", alignItems: "center", background: "#f1f3f4", padding: "10px 20px", borderBottom: "1px solid #ccc", position: "sticky", top: 0, zIndex: 100
        }}>
          <span style={{marginRight: 16, fontWeight: 600}}>{selectedMails.length}</span>
          <button onClick={() => handleMoveToTrash(selectedMails)} title="Trash" style={{background: "none", border: "none", fontSize: 22, marginRight: 12, cursor: "pointer"}}>
            <AiOutlineDelete />
          </button>
          <button onClick={() => handleMoveToSpam(selectedMails)} title="Spam" style={{background: "none", border: "none", fontSize: 22, marginRight: 12, cursor: "pointer"}}>
            <AiOutlineStop />
          </button>
          <button onClick={() => setSelectedMails([])} title="Cancel" style={{background: "none", border: "none", fontSize: 22, marginRight: 12, cursor: "pointer"}}>
            <AiOutlineClose />
          </button>
        </div>
      )}
      <div className="inbox-header-bar" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 10 }}>
        <h2 style={{ margin: 0 }}>Inbox</h2>
      </div>
      <MailListTab onTabSelect={setSelectedTab} />

      {error && <p className="error">{error}</p>}
      {filteredInboxMails.length === 0 && !error && <p>No messages.</p>}
      <ul className="mail-list">
        {filteredInboxMails.map((mail) => (
          <li
            key={mail.id}
            className={`mail-item ${mail.isRead ? "read" : "unread"}`}
            onClick={(e) => handleOpenMail(e, mail)}
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
              onClick={(e) => handleStarredMail(e, mail, setInboxMails)}
            />
            {mail.isDraft && mail.direction.includes("received") && mail.direction.includes("draft") && (
              <span className="mail-label">Draft</span>
            )}

            {(mail.direction.includes("received") || !mail.isDraft) && (
              <span className="mail-from">{mail.from}</span>
            )}

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
                day: "numeric",
              })}
            </span>

            <span
              className="mail-delete"
              onClick={(e) => handleDeleteMail(e, mail.id, setInboxMails)}
              title="Delete"
            >
              <AiOutlineDelete />
            </span>
          </li>
        ))}
      </ul>
      {showDraftEdit && (
        <EditMail
          show={showDraftEdit}
          onClose={() => {
            setShowDraftEdit(false);
            setSelectedDraft(null);
          }}
          addMail={addMail}
          updateDraftMail={updateDraftMail}
          draftMail={selectedDraft}
        />
)}

    </div>
  );
};

export default InboxMail;
