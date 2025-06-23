
import { useState } from "react";
import "./Sidebar.css";
import { useNavigate } from "react-router-dom";
import { NavLink } from "react-router-dom";
import Button from '../../components/Button/Button';
import CreateLabel from "../../popUps/CreateLabel/CreateLabel";
import CreateMail from "../../popUps/CreateMail/CreateMail";
import LabelOptions from "../../popUps/LabelOptions/LabelOptions";
import RemoveLabel from "../../popUps/RemoveLabel/RemoveLabel";
import EditLabel from "../../popUps/EditLabel/EditLabel";

import '../../components/Button/CircleButton.css';
import {
  AiOutlineInbox,
  AiOutlineStar,
  AiOutlineWarning,
  AiOutlineFolderOpen,
  AiOutlineFile,
  AiOutlineEdit,
  AiOutlineArrowRight,
  AiOutlinePlus,
  AiOutlineDoubleRight,
  AiOutlineMore 
} from "react-icons/ai";

const Sidebar = ({ labels, setLabels, addMail, addDraftMail, mailCounts = {}}) => {
  const folders = {
    inbox: { icon: <AiOutlineInbox />, name: "Inbox" },
    starred: { icon: <AiOutlineStar />, name: "Starred" },
    sent: { icon: <AiOutlineArrowRight />, name: "Sent" },
    drafts: { icon: <AiOutlineFile />, name: "Drafts" },
    spam: { icon: <AiOutlineWarning />, name: "Spam" },
    trash: { icon: <AiOutlineFolderOpen />, name: "Trash" }
  };

  const navigate = useNavigate();
  const [showCreateLabel, setCreateLabel] = useState(false);
  const [showLabelOptions, setLabelOptions] = useState(false);
  const [showCreateMail, setCreateMail] = useState(false);
  const [showRemoveLabel, setRemoveLabel] = useState(false);
  const [showEditLabel, setEditLabel] = useState(false);
  const [labelId, setLabelId] = useState(null);
  const [labelName, setLabeName] = useState("");

  return (
    <div className="sidebar">
      <div className="compose-btn" >
        <Button onClick={() => setCreateMail(true)}
            variant = 'blue'>
            <span className="side-icon"><AiOutlineEdit /></span>Compose
        </Button>
      </div>
      <div>
        {Object.entries(folders).map(([path, { icon, name }]) => (
          <NavLink to={`/mails/${path}`} key={path} className="sidebar-box">
            <span className="side-icon">{icon}</span>{name}
            {["inbox", "drafts"].includes(path) && mailCounts[path] > 0 && (
              <span className="count-badge">{mailCounts[path]}</span>
            )}
          </NavLink>
        ))} 
      </div>

      <div className="label-header">
        <span>Labels</span>
        <span className="circle-button" onClick={() => setCreateLabel(true)}><AiOutlinePlus /></span>
      </div>
      <div className="labels-content">
        {labels.map((label) => (
          <div className="sidebar-box" key={label.id} style={{ justifyContent: "space-between" }} onClick={() => navigate(`/mails/label/${label.id}`)}>
            <div style={{ display: "flex", alignItems: "center" }}>
              <span className="side-icon"><AiOutlineDoubleRight /></span>
              {label.name}
            </div>
            <span className="circle-button" onClick={(e) => {e.stopPropagation(); setLabelOptions(true); setLabelId(label.id); setLabeName(label.name);}}><AiOutlineMore /></span>
          </div>
        ))}
      </div>

      <CreateLabel show={showCreateLabel} onClose={() => setCreateLabel(false)} setLabels={setLabels} labels={labels} />
      <LabelOptions show={showLabelOptions} onClose={() => setLabelOptions(false)} setRemoveLabel={setRemoveLabel} setEditLabel={setEditLabel}/>
      <CreateMail show={showCreateMail} onClose={() => setCreateMail(false)} addMail={addMail} addDraftMail={addDraftMail}/>
      <RemoveLabel show={showRemoveLabel} onClose={() => setRemoveLabel(false)} labelId={labelId} setLabels={setLabels} labelName={labelName}/>
      <EditLabel show={showEditLabel} onClose={() => setEditLabel(false)} labelId={labelId} setLabels={setLabels} currentLabelName={labelName} labels={labels}/>
    </div>
  );
};

export default Sidebar;
