import { useState, useRef, useEffect } from "react";
import { useLocation, useOutletContext } from "react-router-dom";
import CreateLabel from "../../popUps/CreateLabel/CreateLabel";
import {
  AiOutlineDelete,
  AiOutlineWarning,
  AiOutlineFolderAdd,
  AiOutlineFolderOpen,
  AiOutlineSearch,
} from "react-icons/ai";
import "./MailActionBar.css";

const MailActionBar = ({
  selected,
  onMarkSpam,
  onDelete,
  onMoveTo
}) => {
  const location = useLocation();
  const isInboxPage = location.pathname.includes("/inbox");
  const isSpamPage = location.pathname.includes("/spam");
  const isTrashPage = location.pathname.includes("/trash");
  const isDraftsPage = location.pathname.includes("/drafts");

  const [showCreateLabel, setCreateLabel] = useState(false);
  const { labels, setLabels } = useOutletContext();
  const [isMenuOpen, setMenuOpen] = useState(false);
  const [search, setSearch] = useState("");
  const menuRef = useRef();

  let labelOptions = [
    ...labels.map((label) => ({ id: label.id, name: label.name })),
    { id: "Social", name: "Social" },
    { id: "Updates", name: "Updates" },
    { id: "Promotions", name: "Promotions" }
  ];

  if (isInboxPage) {
    labelOptions.push({ id: "Spam", name: "Spam" }, { id: "Trash", name: "Trash" });
  } else if (isTrashPage) {
    labelOptions.push({ id: "Inbox", name: "Inbox" }, { id: "Spam", name: "Spam" });
  }
  else if (isSpamPage) {
    labelOptions.push({ id: "Trash", name: "Trash" });
  }

  const filteredLabels = labelOptions.filter((label) =>
    label.name.toLowerCase().includes(search.toLowerCase())
  );

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    };

    if (isMenuOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isMenuOpen]);

  if (selected.length === 0) return null;

  return (
    <div>
      <div className="action-bar">
        <span>{selected.length} selected</span>

        
        {isDraftsPage ? (
          <button title="Move to Inbox" onClick={onMoveTo}>
            
            <AiOutlineFolderOpen />
          </button>
        ) : (
          <button
            title={isSpamPage ? "Unmark as Spam" : "Mark as Spam"}
            onClick={onMarkSpam}
          >
            <AiOutlineWarning />
          </button>
        )}

        <button title="Delete" onClick={onDelete}>
          <AiOutlineDelete />
        </button>

        {(isInboxPage || isSpamPage || isTrashPage) && (
          <div className="move-to-wrapper">
            <button
              title="Move to..."
              onClick={() => setMenuOpen((prev) => !prev)}
            >
              <AiOutlineFolderAdd />
            </button>

            {isMenuOpen && (
              <div className="move-to-menu" ref={menuRef}>
                <div className="move-to-header">
                  Move to:
                  <div className="move-to-search">
                    <input
                      type="text"
                      placeholder=""
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                    />
                    <AiOutlineSearch />
                  </div>
                </div>

                <div className="move-to-divider" />

                {filteredLabels.map((label, index) => (
                  <div
                    key={index}
                    className="move-to-item"
                    onClick={() => {
                      onMoveTo(label.id);
                      setMenuOpen(false);
                      setSearch("");
                    }}
                  >
                    {label.name}
                  </div>
                ))}

                <div className="move-to-divider" />

                <div
                  className="move-to-item"
                  onClick={() => setCreateLabel(true)}
                >
                  Create new
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      <CreateLabel show={showCreateLabel} onClose={() => setCreateLabel(false)} setLabels={setLabels} labels={labels} onMoveTo={onMoveTo} />
    </div>
  );
};

export default MailActionBar;
