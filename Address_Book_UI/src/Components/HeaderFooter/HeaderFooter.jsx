import './HeaderFooter.css'
import logo from "../../Icons/logo.png";
import { Link } from "react-router-dom"

export function AppHeader() {
    return (
        <>
            <Link to="/">
                <header className='app-header prevent-select'>
                    <img src={logo} className="logo" />
                    <h2 className="logo-text">MyYellowBook</h2>
                </header>
            </Link>
        </>
    )
}

export function AppFooter() {
    const year = new Date();
    return (
        <>
            <footer className='app-footer prevent-select'>© {year.getFullYear()} MVI</footer>
        </>
    )
}
