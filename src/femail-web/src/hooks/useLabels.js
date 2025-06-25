import { useState, useEffect } from "react";

export const useGetLabels = (token, userId) => {
  const [labels, setLabels] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/labels/", {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
        "user-id": userId
      }
    })
      .then((res) => {
        if (!res.ok) throw new Error("No labels found");
        return res.json();
      })
      .then(setLabels)
      .catch((err) => setError(err.message));
  }, [token, userId]);

  return { labels, setLabels, error, setError };
};

export const postLabel = async (token, userId, setLabels, labelName, setLabelName, setError) => {
  try {
    const response = await fetch("http://localhost:8080/api/labels", {
      method: "POST",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      },
      body: JSON.stringify({ name: labelName })
    });

    if (!response.ok) throw new Error("Failed to create label");

    const data = await response.json();
    const id = data.id;
    if (id) {
      setLabels((prev) => [...prev, {id: id, name: labelName}]);
      setLabelName("");
      return { id };
    }
    else
      setError("error");
  } catch (err) {
    setError(err.message);
  }
}; 

export const patchLabel = async (token, userId, setLabels, labelId, labelName, setLabelName, setError) => {
  try {
    const response = await fetch(`http://localhost:8080/api/labels/${labelId}`, {
      method: "PATCH",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      },
      body: JSON.stringify({ name: labelName })
    });

    if (!response.ok) throw new Error("Failed to create label");

    setLabels((prev) => 
      prev.map(item =>
        item.id === labelId ? {...item, name: labelName } : item
      ));
    setLabelName("");
  } catch (err) {
    setError(err.message);
  }
}; 

export const deleteLabel = async (token, userId, setLabels, labelId, setError) => {
  try {
    const response = await fetch(`http://localhost:8080/api/labels/${labelId}`, {
      method: "DELETE",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      }
    });

    if (!response.ok) throw new Error("Failed to create label");

    setLabels((prev) => prev.filter(item => item.id !== labelId));
  } catch (err) {
    setError(err.message);
  }
}; 

export const getLabel = async (token, userId, labelId, setLabelName, setLabelError) => {
  try {
    const response = await fetch(`http://localhost:8080/api/labels/${labelId}`, {
      method: "GET",
      headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
          "user-id": userId
      }
    });

    if (!response.ok) throw new Error("Failed to get label");
    const data = await response.json();
    setLabelName(data.name);
  } catch (err) {
    setLabelError(err.message);
  }
}; 