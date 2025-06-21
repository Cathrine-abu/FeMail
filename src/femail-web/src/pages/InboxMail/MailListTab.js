import { useState } from "react";
import "./MailListTab.css";
import {
  AiOutlineInbox,
  AiOutlineTag,
  AiOutlineUsergroupAdd,
  AiOutlineInfoCircle
} from "react-icons/ai";

const tabs = [
  { name: "Primary", icon: <AiOutlineInbox /> },
  { name: "Promotions", icon: <AiOutlineTag /> },
  { name: "Social", icon: <AiOutlineUsergroupAdd /> },
  { name: "Updates", icon: <AiOutlineInfoCircle /> }
];

const MailListTab = ({ onTabSelect }) => {
  const [activeTab, setActiveTab] = useState("Primary");

  const handleClick = (tabName) => {
    setActiveTab(tabName);
    onTabSelect(tabName);
  };

  return (
    <div className="tab-container">
      {tabs.map((tab) => (
        <div
          key={tab.name}
          className={`tab-item ${activeTab === tab.name ? "active" : ""}`}
          onClick={() => handleClick(tab.name)}
        >
          <span className="tab-icon">{tab.icon}</span>
          <span className="tab-label">{tab.name}</span>
        </div>
      ))}
    </div>
  );
};

export default MailListTab;
