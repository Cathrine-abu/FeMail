import { AiOutlineStar, AiFillStar } from "react-icons/ai";

const MailStarButton = ({ isStarred, onClick }) => {
  const handleClick = (e) => {
    e.stopPropagation();
    onClick(e);
  };

  return (
    <span
      className="mail-star"
      onClick={handleClick}
      title={isStarred ? "Unstar" : "Star"}
      style={{ cursor: "pointer" }} 
    >
      {isStarred ? (
        <AiFillStar style={{ color: "gold" }} />
      ) : (
        <AiOutlineStar style={{ color: "gray" }} />
      )}
    </span>
  );
};

export default MailStarButton;
