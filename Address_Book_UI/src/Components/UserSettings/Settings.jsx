import { Link } from 'react-router-dom'
import './Settings.css'


function Settings() {
    return (
        <div className="main-container">
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Settings
                </h2>
            </div>
            <hr style={{ marginTop: "20px" }} />
            <ul className="setting">
                <Link to="./user">
                    <li>User Details</li>
                </Link>
                <Link to="./labels">
                    <li>Labels</li>
                </Link>
                <Link to="./import-export">
                    <li>Import/Export Contacts</li>
                </Link>
                <Link to="./queries">
                    <li>Queries</li>
                </Link>
            </ul>
        </div>
    )
}

export default Settings