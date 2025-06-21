import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./ViewMail.css";
import MailToolbar from "../../components/MailToolbarView/MailToolbarView";
import MailDetails from "../../popUps/MailDetails/MailDetails";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";
import { useGetUserByUsername } from "../../hooks/useUsers";


const MailView = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");

  const [selectedMails, setSelectedMails] = useState([]);
  const [mails, setMails] = useState([]);
  const { handleStarredMail, deleteSelectedMails, markSpam } = useMails(token, userId);
  const { id } = useParams();
  const [mail, setMail] = useState(null);
  const [error, setError] = useState(null);
  const [showDetails, setShowDetails] = useState(false);
  const buttonRef = useRef(null);
  const navigate = useNavigate();
  const currentIndex = 1;
  const total = 1;

  // Fetch sender's user data for profile picture
  const senderUsername = mail?.from;
  const { user: senderUser } = useGetUserByUsername(token, senderUsername);

  useEffect(() => {
    fetch(`http://localhost:8080/api/mails/${id}`, {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("Mail not found");
        return res.json();
      })
      .then((data) => setMail(data))
      .catch((err) => setError(err.message));
  }, [id]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (showDetails && !buttonRef.current?.contains(e.target)) {
        setShowDetails(false);
      }
    };
    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, [showDetails]);

  if (error) return <div>Error: {error}</div>;
  if (!mail) return <div>Loading mail...</div>;

  return (
  <div className="view-mail-wrapper">
    <MailToolbar
      onBack={() => navigate(-1)}
      onDelete={() => {
        deleteSelectedMails([mail.id], setMails, setSelectedMails);
        navigate(-1); 
      }}
      onMarkSpam={() => {
        markSpam([mail.id], [mail], setMails, setSelectedMails);
        navigate(-1);
      }}

      onMoveTo={() => null}
      onNext={() => null}
      onPrev={() => null}
      currentIndex={currentIndex}
      total={total}
    />

    <div className="view-mail-container">
      <div className="view-mail-header">
        <h1 className="view-mail-subject">{mail.subject}</h1>
      </div>

      <div className="mail-sender-info">
        <div className="mail-from-line">
          <div className="mail-left-side">
            <div className="mail-avatar">
              {senderUser?.image ? (
                <img
                  src={senderUser.image}
                  alt="Sender profile"
                  style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }}
                />
              ) : (
                mail.from?.charAt(0).toUpperCase() || "?"
              )}
            </div>

            <div className="mail-name-with-toggle">
              <strong className="mail-from-text">{mail.from}</strong>

              {/* In your MailView component, replace the details section with: */}
              <div className="details-wrapper" ref={buttonRef} style={{display: 'inline-block'}}>
                <button
                  className="details-toggle"
                  onClick={() => setShowDetails(!showDetails)}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    padding: '0 4px',
                    fontSize: '12px'
                  }}
                >
                {'▼'}
                </button>
                {showDetails && <MailDetails mail={mail} onClose={() => setShowDetails(false)} />}
              </div>
            </div>
          </div>

          <div className="mail-right-side">
            <span className="mail-date-text">
              {new Date(mail.timestamp).toLocaleString("en-US", {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
              })}
            </span>
            <MailStarButton
              isStarred={mail.isStarred}
              onClick={(e) => {
                handleStarredMail(e, mail, () => {
                  setMail((prev) => ({ ...prev, isStarred: !prev.isStarred }));
                });
              }}
            />
          </div>
        </div>
      </div>

      <div className="view-mail-body">
        <p>{mail.body}</p>
      </div>
    </div>
  </div>
);

};

export default MailView;
