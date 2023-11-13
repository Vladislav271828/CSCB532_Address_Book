import './HeaderFooter.css'
import logo from "./logo400.png";

export function AppHeader() {
    return (
        <>
            <header className='app-header prevent-select'>
                <img src={logo} className="logo" />
                <h2 className="logo-text">MyYellowBook</h2>
            </header>
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
