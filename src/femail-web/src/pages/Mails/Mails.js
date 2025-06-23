
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../Sidebar/Sidebar";
import Topbar from "../Topbar/Topbar";
import { Outlet } from "react-router-dom";
import { useGetUser } from "../../hooks/useUsers";
import { useGetLabels } from "../../hooks/useLabels";
import "./Mails.css";


const Mail = () => {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem("userId");
  const [mailCounts, setMailCounts] = useState({});
  
  useEffect(() => {
    // Check if user is authenticated
    if (!token) {
      navigate('/');
      return;
    }

    // Verify token is still valid
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      
      if (payload.exp < currentTime) {
        localStorage.removeItem('token');
        navigate('/');
      }
    } catch (error) {
      localStorage.removeItem('token');
      navigate('/');
    }
  }, [token, navigate]);
  

  const { user, } = useGetUser(token, userId);
  const [inboxMails, setInboxMails] = useState([]);
  const [sendMails, setSendMails] = useState([]);
  const [draftMails, setDraftMails] = useState([]);
  const [spamMails, setSpamMails] = useState([]);
  const [starredMails, setStarredMails] = useState([]);
  const { labels, setLabels, } = useGetLabels(token, userId);

  useEffect(() => {
    setMailCounts({
      inbox: inboxMails.length,
      drafts: draftMails.length,
    });
  }, [inboxMails, draftMails]);


  const addMail = (newMail) => {
    if (newMail.to.includes(user.username) && newMail.isSpam === "false") {
       setInboxMails((prev) => [...prev, newMail]);
     }
    if (newMail.isSpam === "true") {
      setSpamMails((prev) => [...prev, newMail]);
    }
    if (newMail.isSpam === "false") {
      setSendMails((prev) => [...prev, newMail]);
    }
    const wasDraft = draftMails.find(mail => mail.id === newMail.id);
    if (wasDraft) {
      setDraftMails(prev => prev.filter(mail => mail.id !== newMail.id));
    }
  }

  const addDraftMail = (newMail) => {
    if (!newMail) return;
    setDraftMails((prev) => [...prev, newMail]);
  };

  const updateDraftMail = (newMail) => {
    if (!newMail) return;
    setDraftMails(prev => prev.filter(mail => mail.id !== newMail.id));
    setDraftMails((prev) => [...prev, newMail]);
  };


  return (
    <div className="mail-screen">
      <Topbar />
      <div className="mail-side">
        <Sidebar addMail={addMail} addDraftMail={addDraftMail}
            updateDraftMail={updateDraftMail} mailCounts={mailCounts}
            labels={labels} setLabels={setLabels}/>
        <div className="mail-container">
          <Outlet context={{inboxMails, setInboxMails,
                            sendMails, setSendMails,
                            draftMails, setDraftMails,
                            spamMails, setSpamMails,
                            starredMails, setStarredMails,
                            labels, setLabels,
                            addMail, addDraftMail, updateDraftMail}}/>
        </div>
      </div>
    </div>
  );
};

export default Mail;
