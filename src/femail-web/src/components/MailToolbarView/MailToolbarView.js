import {
  AiOutlineArrowLeft,
  AiOutlineDelete,
  AiOutlineExclamationCircle
} from "react-icons/ai";
import "./MailToolbarView.css";

const MailToolbar = ({
  onBack,
  onDelete,
  onMarkSpam
}) => {
  return (
    <div className="mail-toolbar">
      <div className="toolbar-left">
        <button onClick={onBack} title="Back"><AiOutlineArrowLeft /></button>
        <button onClick={onMarkSpam} title="Mark as spam"><AiOutlineExclamationCircle /></button>
        <button onClick={onDelete} title="Delete"><AiOutlineDelete /></button>
      </div>
    </div>
  );
};

export default MailToolbar;
