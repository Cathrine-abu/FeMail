import "./RemoveLabel.css";
import '../../components/PopUp/PopUp.css';
import '../../components/Button/CircleButton.css';
import Button from '../../components/Button/Button';
import { deleteLabel } from "../../hooks/useLabels";


const RemoveLabel = ({ show, onClose, labelId, setLabels, labelName }) => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    if (!show) return null;

    return (
        <div className="pop-up-overlay remove-label-overlay">
            <div className="pop-up-box remove-label-box">
                <div className="remove-label-header">
                    Remove Label
                </div>
                <div className="remove-label-content">Delete the Label <b>"{labelName}"</b>?</div>
                <div className="remove-label-button-raw">
                    <Button onClick={onClose}
                        variant = 'grey'>
                        Cancel
                    </Button>
                    <Button onClick={() => {deleteLabel(token, userId, setLabels, labelId); onClose();}}
                        variant = 'blue'>
                        Delete
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default RemoveLabel;
