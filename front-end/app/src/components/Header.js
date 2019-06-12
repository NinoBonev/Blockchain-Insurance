import React from 'react'
import {Link} from 'react-router-dom'

import '../style/app.css'


function Header() {
    return (
    <div style={{height : '50px', backgroundColor: 'black', align: 'centre'}}>
        <Link to={{pathname: `/`}} className='no-decoration'>
            <span className='header-text' style={{marginLeft: '40px'}}>Home</span>
        </Link>

        <Link to={{pathname: `/sign-insurance`}} className='no-decoration'>
            <span className='header-text' style={{marginLeft: '800px'}} >Sign insurance</span>
        </Link>

        <Link to={{pathname: `/pay-insurance`}} className='no-decoration'>
        <span className='header-text' style={{marginLeft: '20px'}}>Pay insurance</span>
        </Link>

        <Link to={{pathname: `/check-status`}} className='no-decoration'>
        <span className='header-text' style={{marginLeft: '20px'}}>Check status</span>
        </Link>
    </div>
    )
}

export default Header;