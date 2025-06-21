import { useState, useEffect } from "react";

export const useGetMail = (token, userId) => {
  const [mail, setMail] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/mails/", {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("No mails found");
        return res.json();
      })
      .then(setMail)
      .catch((err) => setError(err.message));
  }, [userId]);

  return { mail, error, setError };
};

export const postMail = async (token, userId, mailSubject, mailBody,mailFrom, mailTo, addMail, setError,isDraft = false) => {
  try {
    let direction = ["sent"];
    const userResponse = await fetch(`http://localhost:8080/api/users/${userId}`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
        "user-id": userId
      }
    });

    if (!userResponse.ok) throw new Error("Failed to get user");
    const user = await userResponse.json();

    if (isDraft) {
      direction = ["draft"];
    } else if (
      mailTo === userId || 
      (Array.isArray(mailTo) && user?.username && mailTo.includes(user.username))
    ) {
      direction = ["sent", "received"];
    }

    const response = await fetch("http://localhost:8080/api/mails", {
      method: "POST",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      },

      body: JSON.stringify({ subject: mailSubject, body: mailBody, from: userId, to: mailTo, isDraft: isDraft, direction})
    });

    if (!response.ok) throw new Error("Failed to create mail");

    const data = await response.json();
    const id = data.id;
    const isSpam = data.isSpam;
    const timestamp = data.timestamp;
    if (id) {
      addMail({id: id, subject: mailSubject, body: mailBody, from: mailFrom, to: mailTo, isSpam: isSpam, timestamp: timestamp, isDraft: isDraft, direction}); 
    }
    else
      setError("error");
  } catch (err) {
    setError(err.message);
  }
}; 

export const patchMail = async (token, userId, mailId, mailSubject, mailBody, mailFrom, mailTo, addMail, setError, isDraft= true) => {
  try {
    let direction = ["sent"];
    const userResponse = await fetch(`http://localhost:8080/api/users/${userId}`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
        "user-id": userId
      }
    });

    if (!userResponse.ok) throw new Error("Failed to get user");
    const user = await userResponse.json();

    if (isDraft) {
      direction = ["draft"];
    } else if (
      mailTo === userId || 
      (Array.isArray(mailTo) && user?.username && mailTo.includes(user.username))
    ) {
      direction = ["sent", "received"];
    }
    const response = await fetch(`http://localhost:8080/api/mails/${mailId}`, {
      method: "PATCH",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      },
      body: JSON.stringify({ subject: mailSubject, body: mailBody, from: mailFrom, to: mailTo, isDraft,
        direction: direction })
    });

    if (!response.ok) throw new Error("Failed to update mail");

    const data = await response.json();

    const id = data.id;
    const isSpam = data.isSpam;
    const timestamp = data.timestamp;
    if (id) {
      addMail({id: id, subject: mailSubject, body: mailBody, from: mailFrom, to: mailTo, isSpam: isSpam, timestamp: timestamp, isDraft: isDraft, direction}); 
    }
    else
      setError("error");
  } catch (err) {
    setError(err.message);
  }
}; 

export const useMails = (token, userId) => {
  const handleDeleteMail = (e, mailId, setMails) => {
    e.stopPropagation();
    fetch(`http://localhost:8080/api/mails/${mailId}`, {
      method: "PATCH",
      headers: {
        "user-id": userId,
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ isDeleted: true }),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to delete mail");
        setMails((prev) => prev.filter((mail) => mail.id !== mailId));
      })
      .catch((err) => {
        console.error("Delete failed", err);
      });
  };

  const deleteSelectedMails = (selectedMails, setMails, setSelectedMails) => {
    selectedMails.forEach((id) => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: {
          "user-id": userId,
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ isDeleted: true }),
      });
    });

    setMails((prev) => prev.filter((mail) => !selectedMails.includes(mail.id)));
    setSelectedMails([]);
  };

  const permanentDeleteSelectedMails = (selectedMails, setMails, setSelectedMails) => {
    selectedMails.forEach((id) => {
      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "DELETE",
        headers: {
          "user-id": userId,
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ isDeleted: false }),
      });
    });

    setMails((prev) => prev.filter((mail) => !selectedMails.includes(mail.id)));
    setSelectedMails([]);
  };

  const markSpam = (selectedMails, mails, setMails, setSelectedMails) => {
    selectedMails.forEach((id) => {
      const mail = mails.find((m) => m.id === id);
      if (!mail) return;

      fetch(`http://localhost:8080/api/mails/${id}`, {
        method: "PATCH",
        headers: {
          "user-id": userId,
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ isSpam: true, isDeleted: false}),
      });

      const combinedText = `${mail.subject} ${mail.body}`;
      const links =
        combinedText.match(/(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g) || [];

      links.forEach((link) => {
        fetch("http://localhost:8080/api/blacklist", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify({ url: link }),
        });
      });
    });

    setMails((prev) =>
      prev.filter((mail) => !selectedMails.includes(mail.id))
    );
    setSelectedMails([]);
  };

  const handleStarredMail = (e, mail, setMails) => {
    e.stopPropagation();
    const updated = { ...mail, isStarred: !mail.isStarred };

    fetch(`http://localhost:8080/api/mails/${mail.id}`, {
      method: "PATCH",
      headers: {
        "user-id": userId,
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ isStarred: updated.isStarred }),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to update star");
        setMails((prev) =>
          prev.map((m) => (m.id === mail.id ? updated : m))
        );
      })
      .catch((err) => {
        console.error("Star toggle failed", err);
      });
  };

  const unMarkSpam = (selectedMails, spamMails, setSpamMails, setSelectedMails) => {
  selectedMails.forEach((id) => {
    const mail = spamMails.find((m) => m.id === id);
    if (!mail) return;

    fetch(`http://localhost:8080/api/mails/${id}`, {
      method: "PATCH",
      headers: {
        "user-id": userId,
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ isSpam: false })
    });

    const combinedText = `${mail.subject} ${mail.body}`;
    const links = combinedText.match(/(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g) || [];

    links.forEach((link) => {
      const encoded = encodeURIComponent(link);
      fetch(`http://localhost:8080/api/blacklist/${encoded}`, {
        method: "DELETE",
        headers: {
        "Authorization": `Bearer ${token}`
      }
      });
    });
  });

  
  setSpamMails((prev) => prev.filter((mail) => !selectedMails.includes(mail.id)));
  setSelectedMails([]);
};


const toggleStar = (token, e, mail, setStarredMails) => {
  e.stopPropagation();
  const updated = { ...mail, isStarred: !mail.isStarred };

  fetch(`http://localhost:8080/api/mails/${mail.id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
      "user-id": userId
    },
    body: JSON.stringify({ isStarred: updated.isStarred })
  })
    .then((res) => {
      if (!res.ok) throw new Error("Failed to update star");

      setStarredMails((prev) =>
        updated.isStarred
          ? [...prev, updated]
          : prev.filter((m) => m.id !== mail.id)
      );
    })
    .catch((err) => {
      console.error("Failed to toggle star:", err);
    });
};


  return {
    handleDeleteMail,
    deleteSelectedMails,
    markSpam,
    handleStarredMail,
    unMarkSpam,
    toggleStar,
    permanentDeleteSelectedMails,
  };
};
