import { useState, useEffect } from "react";

export const useGetSearch = (token, userId, query) => {
  const [searchMails, setSearchMails] = useState([]);
  const [error, setError] = useState(null);
  useEffect(() => {
    fetch(`http://localhost:8080/api/mails/search/${query}/`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("No mails found");
        return res.json();
      })
      .then((data) => setSearchMails(data))
      .catch((err) => setError(err.message));
  }, [token, userId, query]);

  return { searchMails, setSearchMails, error, setError };
};
