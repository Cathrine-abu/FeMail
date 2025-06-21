import "./LabelOptions.css";
import '../../components/PopUp/PopUp.css';
import '../../components/Button/CircleButton.css';
import { AiOutlineClose } from "react-icons/ai";

const LabelOptions = ({ show, onClose, setRemoveLabel, setEditLabel }) => {

    if (!show) return null;

    return (
        <div className="pop-up-overlay">
            <div className="pop-up-box label-options-box">
                <div className="label-options-header">
                    <span></span><span className="circle-button" onClick={onClose}><AiOutlineClose  /></span>
                </div>
                <div className="label-options-content" onClick={() => {onClose(); setEditLabel(true);}}>Edit label</div>
                <div className="label-options-content" onClick={() => {onClose(); setRemoveLabel(true);}}>Delete label</div>
            </div>
        </div>
    );
};

export default LabelOptions;
