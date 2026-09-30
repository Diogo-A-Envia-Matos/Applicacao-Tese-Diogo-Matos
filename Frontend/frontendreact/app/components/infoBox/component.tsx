import { FaInfoCircle } from 'react-icons/fa';
import './Component.css';

export default function InfoBox({message = 'There is currently an assignment being computed, so the information in this page might change later.'}) {
  return (
    <div className="info-message">
        <FaInfoCircle className="info-icon" />
        {message}
    </div>
  );
}
