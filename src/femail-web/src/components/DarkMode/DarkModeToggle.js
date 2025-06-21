import "./DarkModeToggle.css";
import { AiOutlineMoon, AiOutlineSun } from "react-icons/ai";

function DarkModeToggle({ isDark, onChange }) {
  return (
    <label className="toggle-switch">
      <input type="checkbox" checked={!isDark} onChange={onChange} />
      <span className="slider" >
        {isDark ? <AiOutlineMoon className="icon-moon" /> : <AiOutlineSun className="icon-sun" />}
      </span>
    </label>
  );
}

export default DarkModeToggle;