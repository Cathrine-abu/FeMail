import "../../components/MailRowsStyle/MailRowsStyle.css";
import MailActionBar from "../../components/MailActionBar/MailActionBar";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AiOutlineDelete } from "react-icons/ai";
import MailStarButton from "../../components/MailStarButton/MailStarButton";
import { useMails } from "../../hooks/useMails";
import { useGetSearch } from "../../hooks/useSearch";

const SearchMail = () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const { query } = useParams();
  const [selectedMails, setSelectedMails] = useState([]);
  const navigate = useNavigate();

  const { deleteSelectedMails, markSpam, handleStarredMail, handleDeleteMail } = useMails(token, userId);
  const { searchMails, setSearchMails, error, } = useGetSearch(token, userId, query);
  
  const handleDeleteSelected = () => {
    deleteSelectedMails(selectedMails, setSearchMails, setSelectedMails);
  };

  const handleMarkSpam = () => {
    markSpam(selectedMails, searchMails, setSearchMails, setSelectedMails);
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
    navigate(`/mails/search/${query}/${mailId}`);
  };

  return (
    <div className="search-container">
      <MailActionBar
        selected={selectedMails}
        onDelete={handleDeleteSelected}
        onMarkSpam={handleMarkSpam}
        onClear={() => setSelectedMails([])}
      />

      {error && <p className="error">{error}</p>}
      {searchMails.length === 0 && !error && <p>No messages found.</p>}

      <ul className="mail-list">
        {searchMails.map((mail) => (
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
              onClick={(e) => handleStarredMail(e, mail, setSearchMails)}
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
              onClick={(e) => handleDeleteMail(e, mail.id, setSearchMails)}
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

export default SearchMail;
