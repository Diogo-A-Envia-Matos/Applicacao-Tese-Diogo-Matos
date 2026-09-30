import { FaInfoCircle } from 'react-icons/fa';
import './Component.css';

export default function ComputationWarning({message = 'Since there is currently an assignment being computed, you are not allowed to change restrictions.'}) {
  return (
    <div className="warning-message">
        <FaInfoCircle className="warning-icon" />
        {message}
    </div>
  );
}
