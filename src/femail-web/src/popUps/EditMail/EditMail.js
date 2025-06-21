import { useState, useEffect } from "react";
import '../../components/PopUp/PopUp.css';
import '../../components/PopUp/CreateEditMail.css';
import '../../components/Button/CircleButton.css';
import Button from '../../components/Button/Button';
import { AiOutlineClose } from "react-icons/ai";
import { patchMail } from "../../hooks/useMails";

const EditMail = ({ show, onClose, addMail, updateDraftMail, draftMail }) => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const [, setError] = useState(null);
    const [mailTo, setMailTo] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [mailSubject, setMailSubject] = useState("");
    const [mailBody, setMailBody] = useState("");

    useEffect(() => {
        setMailTo(draftMail.to);
        setMailSubject(draftMail.subject);
        setMailBody(draftMail.body);
    }, [draftMail]);

    const handleKeyDown = (e) => {
        if (e.key === "Enter" && inputValue.trim()) {
            e.preventDefault();
            addRecipient(inputValue.trim());
        }
    };

    const handleBlur = () => {
        if (inputValue.trim()) {
            addRecipient(inputValue.trim());
        }
    };

    const addRecipient = (email) => {
        if (email && !mailTo.includes(email)) {
            const cleanEmail = email.endsWith('@femail.com') ? email.replace('@femail.com', '') : email;
            setMailTo([...mailTo, cleanEmail]);
            setInputValue("");
        }
    };

    const removeRecipient = (email) => {
        setMailTo(mailTo.filter((item) => item !== email));
    };

    if (!show) return null;

    return (
        <div className="pop-up-overlay create-edit-mail-overlay">
            <div className="pop-up-box create-edit-mail-box">
                <div className="compose-container">
                    <div className="create-edit-mail-header">
                        <span>Edit Message</span>
                        <span className="circle-button" onClick={onClose}><AiOutlineClose  /></span>
                    </div>
                    <div className="compose-field to-field">
                        <span className="compose-label">To</span>
                        {mailTo.map((email) => (
                            <div key={email} className="recipient-pill">
                            {email}
                            <button
                                className="remove-btn"
                                onClick={() => removeRecipient(email)}
                                aria-label={`Remove ${email}`}
                            >×</button>
                            </div>
                        ))}
                        <input className="compose-input-line"
                            type="text"
                            placeholder="Recipient"
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyDown={handleKeyDown}
                            onBlur={handleBlur}
                            autoComplete="off"
                        />
                    </div>
                    <div className="compose-field">
                        <label className="compose-label">Subject</label>
                        <input className="compose-input-line"
                            type="text"
                            placeholder="Subject"
                            value={mailSubject}
                            onChange={(e) => setMailSubject(e.target.value)}
                        />
                    </div>
                    <div className="compose-body">
                        <textarea placeholder="Write your message here..."
                            value={mailBody}
                            onChange={(e) => setMailBody(e.target.value)}
                        />
                    </div>
                    <div className="mail-button-raw">
                        <Button variant = 'grey'
                            onClick={() => { patchMail(token, userId, draftMail.id, mailSubject, mailBody, userId, mailTo, updateDraftMail, setError, true );
                                            setMailTo([]); setInputValue(""); setMailSubject(""); setMailBody(""); onClose();}}>
                            Save as draft
                        </Button>
                        <Button variant = 'blue'
                            onClick={() => { patchMail(token, userId, draftMail.id, mailSubject, mailBody, userId, mailTo, addMail, setError, false);
                                            setMailTo([]); setInputValue(""); setMailSubject(""); setMailBody(""); onClose();}}
                            disabled={mailTo.length === 0}>
                            Send
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EditMail;
