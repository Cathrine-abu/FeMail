import { useState, useMemo, useEffect, useCallback } from "react";
import '../../components/PopUp/PopUp.css';
import '../../components/PopUp/CreateEditLabel.css';
import Button from '../../components/Button/Button';
import { patchLabel } from "../../hooks/useLabels";

const EditLabel = ({ show, onClose, setLabels, labels, labelId, currentLabelName }) => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const [labelName, setLabelName] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        setLabelName(currentLabelName);
    }, [currentLabelName]);

    const labelExists = useCallback((labels, labelName) => {
        if (!Array.isArray(labels)) return false;
        return labels.some(label =>
            label.name.toLowerCase() === labelName.trim().toLowerCase() &&
            label.name.toLowerCase() !== currentLabelName.trim().toLowerCase());
    }, [currentLabelName]);

    const isDisabled = useMemo(() => {
        return !labelName.trim() || labelExists(labels, labelName);
    }, [labels, labelName, labelExists]);

    useEffect(() => {
        if (!labelName.trim()) {
            setError("Label name must not be empty");
        } else if (labelExists(labels, labelName)) {
            setError("Label already exists.");
        } else {
            setError(null);
        }
    }, [labelName, labels, setError, labelExists]);


    if (!show) return null;

    return (
        <div className="pop-up-overlay create-edit-label-overlay">
            <div className="pop-up-box create-edit-label-box">
                <div className="create-edit-label-header">Edit Label</div>
                <div className="create-edit-label-content">Label name:</div>
                <input className="input-box"
                    type="text"
                    placeholder="Label name"
                    value={labelName}
                    onChange={(e) => setLabelName(e.target.value)}
                />
                <div className="field-error">{error ? error : "\u00A0"}</div>
                <div className="label-button-raw">
                    <Button onClick={onClose}
                        variant = 'grey'>
                        Cancel
                    </Button>
                    <Button onClick={() => {patchLabel(token, userId, setLabels, labelId, labelName, setLabelName, setError); onClose();}}
                        variant = 'blue'
                        disabled={isDisabled}>
                        Save
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default EditLabel;
