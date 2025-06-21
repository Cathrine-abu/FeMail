import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails, useGetMail } from "../../hooks/useMails";
import { getLabel } from "../../hooks/useLabels";

const LabelMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { labelId } = useParams();
  const navigate = useNavigate();

  const [labelName, setLabelName] = useState(null);
  const [labelError, setLabelNError] = useState(null);
  const [labelMails, setLabelMails] = useState([]);
  const [selectedMails, setSelectedMails] = useState([]);

  const { mail, error, setError } = useGetMail(token, userId);
  const {
    deleteSelectedMails,
    markSpam,
    handleStarredMail,
    handleDeleteMail,
  } = useMails(token, userId);

  useEffect(() => {
    getLabel(
      token,
      userId,
      labelId,
      (name) => {
        setLabelName(name);
      },
      (err) => {
        setLabelNError(err);
      }
    );
  }, [labelId]);

  useEffect(() => {

    if (labelName && Array.isArray(mail)) {
      const filtered = mail.filter((m) => {
        return m.category === labelName && !m.isSpam && !m.isDeleted;
      });

      setLabelMails(filtered);
    }
  }, [labelName, mail]);

  const handleDeleteSelected = () => {
    deleteSelectedMails(selectedMails, setLabelMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, labelMails, setLabelMails, setSelectedMails);
  };

  const handleCheckboxChange = (e, id) => {
    e.stopPropagation();
    setSelectedMails((prev) =>
      e.target.checked ? [...prev, id] : prev.filter((mid) => mid !== id)
    );
  };

  const handleOpenMail = (e, mailId) => {
    const ignore = ["BUTTON", "SVG", "PATH", "INPUT"];
    if (!ignore.includes(e.target.tagName)) {
      navigate(`/mails/label/${labelId}/${mailId}`);
    }
  };

  if (labelError) return <p className="error">{labelError}</p>;

  return (
    <div className="label-container">
      <MailActionBar
        selected={selectedMails}
        onDelete={handleDeleteSelected}
        onMarkSpam={handleMarkSpam}
        onClear={() => setSelectedMails([])}
      />

      {error && <p className="error">{error}</p>}
      {labelMails.length === 0 && !error && <p>No messages found.</p>}

      <ul className="mail-list">
        {labelMails.map((mail) => (
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
              onClick={(e) => handleStarredMail(e, mail, setLabelMails)}
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
                day: "numeric",
              })}
            </span>

            <span
              className="mail-delete"
              onClick={(e) => handleDeleteMail(e, mail.id, setLabelMails)}
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

export default LabelMail;
