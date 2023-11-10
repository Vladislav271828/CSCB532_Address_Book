import './HeaderFooter.css'
import logo from "./logo400.png";

export function AppHeader() {    
    return (
    <>
    <header className='app-header'>
        <img src={ logo } className="logo" />
        <h2 className="logo-text">MyYellowBook</h2>
    </header>
    </>
    )
}

export function AppFooter() {    
    return (
    <>
    <footer className='app-footer'>© MVI</footer>
    </>
    )
}
