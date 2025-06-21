import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import "./App.css";
import Mail from "./pages/Mails/Mails";
import Register from "./pages/Registration/Register";
import InboxMail from "./pages/InboxMail/InboxMail";
import SendMail from "./pages/SendMail/SendMail";
import DraftMail from "./pages/DraftMail/DraftMail";
import SpamMail from "./pages/SpamMail/SpamMail";
import ViewMail from "./pages/ViewMail/ViewMail";
import TrashMail from "./pages/TrashMail/TrashMail";
import StarredMail from "./pages/StarredMail/StarredMail";
import SearchMail from "./pages/SearchMail/SearchMail";
import LabelMail from "./pages/LabelMail/LabelMail";
import Login from "./pages/Login/Login"; 


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="*" element={<Navigate to="/" replace />} />
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/mails" element={<Mail />}>
          <Route index element={<Navigate to="inbox" replace />} />
          <Route path="inbox" element={<InboxMail />} />
          <Route path="sent" element={<SendMail />} />
          <Route path="drafts" element={<DraftMail />} />
          <Route path="spam" element={<SpamMail />} />
          <Route path=":folder/:id" element={<ViewMail />} />
          <Route path="trash" element={<TrashMail />} />
          <Route path="starred" element={<StarredMail />} />
          <Route path="search/:query" element={<SearchMail />} />
          <Route path="search/:query/:id" element={<ViewMail />} />
          <Route path="label/:labelId" element={<LabelMail />} />
          <Route path="label/:labelId/:id" element={<ViewMail />} />
          <Route path="*" element={<Navigate to="/mails/inbox" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
