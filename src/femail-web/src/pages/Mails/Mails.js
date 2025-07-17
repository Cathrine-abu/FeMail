
import { useEffect, useState, useCallback } from "react";
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
  const [trashMails, setTrashMails] = useState([]);
  const { labels, setLabels, } = useGetLabels(token, userId);

  // Fetch all mails from server
  const fetchAllMails = useCallback(() => {
    if (!token || !userId) return;
    
    fetch("http://localhost:8080/api/mails", {
      headers: {
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch mails");
        return res.json();
      })
      .then((data) => {
        if (!Array.isArray(data)) {
          console.error("Expected an array but got:", data);
          return;
        }

        // Filter mails by type with stable sorting
        const inbox = data.filter(mail => 
          Array.isArray(mail.direction) &&
          mail.direction.includes("received") &&
          (mail.isDeleted === false || mail.isDeleted === undefined) &&
          mail.isSpam === false
        ).sort((a, b) => {
          // Primary sort by timestamp (newest first)
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          // Secondary sort by ID for stability
          return a.id.localeCompare(b.id);
        });
        
        const sent = data.filter(mail => 
          mail.direction.includes("sent") && 
          (mail.isDeleted === false || mail.isDeleted === undefined)
        ).sort((a, b) => {
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          return a.id.localeCompare(b.id);
        });
        
        const drafts = data.filter(mail => 
          mail.direction.includes("draft") &&
          (mail.isDeleted === false || mail.isDeleted === undefined) &&
          mail.isDraft === true
        ).sort((a, b) => {
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          return a.id.localeCompare(b.id);
        });
        
        const spam = data.filter(mail => 
          mail.isSpam === true &&
          (mail.isDeleted === false || mail.isDeleted === undefined)
        ).sort((a, b) => {
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          return a.id.localeCompare(b.id);
        });
        
        const starred = data.filter(mail => 
          mail.isStarred === true &&
          (mail.isDeleted === false || mail.isDeleted === undefined)
        ).sort((a, b) => {
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          return a.id.localeCompare(b.id);
        });
        
        const trash = data.filter(mail => 
          mail.isDeleted === true
        ).sort((a, b) => {
          const timeDiff = new Date(b.timestamp) - new Date(a.timestamp);
          if (timeDiff !== 0) return timeDiff;
          return a.id.localeCompare(b.id);
        });

        // Merge fetched mails with current state to preserve optimistic mails
        setInboxMails(prev => {
          const prevNotInFetch = prev.filter(pm => !inbox.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...inbox].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
        setSendMails(prev => {
          const prevNotInFetch = prev.filter(pm => !sent.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...sent].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
        setDraftMails(prev => {
          const prevNotInFetch = prev.filter(pm => !drafts.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...drafts].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
        setSpamMails(prev => {
          const prevNotInFetch = prev.filter(pm => !spam.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...spam].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
        setStarredMails(prev => {
          const prevNotInFetch = prev.filter(pm => !starred.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...starred].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
        setTrashMails(prev => {
          const prevNotInFetch = prev.filter(pm => !trash.some(sm => sm.id === pm.id));
          return [...prevNotInFetch, ...trash].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        });
      })
      .catch((err) => {
        console.error("Error fetching mails:", err);
      });
  }, [token, userId]);

  useEffect(() => {
    fetchAllMails();
    // Refresh every 1 seconds for near real-time updates
    const interval = setInterval(() => {
      fetchAllMails();
    }, 10);
    return () => {
      clearInterval(interval);
    };
  }, [fetchAllMails]);

  useEffect(() => {
    setMailCounts({
      inbox: inboxMails.length,
      starred: starredMails.length,
      sent: sendMails.length,
      drafts: draftMails.length,
      spam: spamMails.length,
      trash: trashMails.length
    });
  }, [inboxMails, starredMails, sendMails, draftMails, spamMails, trashMails]);


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
    setDraftMails(prev =>
      prev.map(mail =>
        mail.id === newMail.id
          ? { ...mail, ...newMail }
          : mail
    ));
    const exists = inboxMails.some(mail => mail.id === newMail.id);
    if (exists) {
      setInboxMails(prev =>
        prev.map(mail =>
          mail.id === newMail.id
            ? { ...mail, ...newMail, direction: ['draft', 'received'] }
            : mail
      ));
    }
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
                            trashMails, setTrashMails,
                            labels, setLabels,
                            addMail, addDraftMail, updateDraftMail}}/>
        </div>
      </div>
    </div>
  );
};

export default Mail;
