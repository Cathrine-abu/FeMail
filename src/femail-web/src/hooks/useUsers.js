import { useState, useEffect } from "react";

export const useGetUser = (token, userId) => {
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => {
    fetch(`http://localhost:8080/api/users/${userId}`, {
      headers: {
          "Authorization": `Bearer ${token}`
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("No users found");
        return res.json();
      })
      .then((data) => setUser(data))
      .catch((err) => setError(err.message));
  }, [userId]);

  return { user, error };
};

export const useGetUserByUsername = (token, username) => {
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => {
    if (!username) return;
    fetch(`http://localhost:8080/api/users/username/${username}`, {
      headers: {
          "Authorization": `Bearer ${token}`
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("No users found");
        return res.json();
      })
      .then((data) => setUser(data))
      .catch((err) => setError(err.message));
  }, [username]);

  return { user, error };
};
