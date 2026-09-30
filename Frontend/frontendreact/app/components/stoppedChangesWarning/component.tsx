import { FaInfoCircle } from 'react-icons/fa';
import './Component.css';

export default function StoppedChangesWarning({message = 'Performing changes on the base restrictions is currently forbidden.'}) {
  return (
    <div className="stopped-changes-container">
      <div className="stopped-changes-header">
        <div className="stopped-changes-message">
          <FaInfoCircle className="stopped-changes-icon" />
          {message}
        </div>
      </div>
    </div>
  );
}
