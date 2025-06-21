import { useEffect, useRef } from "react";
import "./MailDetails.css";

const MailDetails = ({ mail, onClose }) => {
  const popupRef = useRef();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (popupRef.current && !popupRef.current.contains(e.target)) {
        onClose();
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [onClose]);

  return (
    <div ref={popupRef} className="mail-details-popup">
      <p><strong>from:</strong> {mail.from}</p>
      <p><strong>to:</strong> {
        Array.isArray(mail.to) ? mail.to.join(", ") : mail.to
      }</p>
      <p><strong>date:</strong> {new Date(mail.timestamp).toLocaleString()}</p>
      <p><strong>subject:</strong> {mail.subject}</p>
      <p><strong>mailed-by:</strong> femail.com</p>
    </div>
  );
};

export default MailDetails;
