import React from 'react'
import {Link} from 'react-router-dom'


function Header() {
    return (
        <div className='header' style={{backgroundColor: '#91d5ff', marginTop: '13px'}}>
                <Link to={{pathname: `/sign-insurance/car-insurance`}} className='no-decoration'>
                    <span className='header-text' style={{marginLeft: '25%'}}>Car Insurance</span>
                </Link>

                <Link to={{pathname: `/sign-insurance/home-insurance`}} className='no-decoration'>
                    <span className='header-text' style={{marginLeft: '20px'}} >Home insurance</span>
                </Link>

                <Link to={{pathname: `/sign-insurance/travel-insurance`}} className='no-decoration'>
                <span className='header-text' style={{marginLeft: '20px'}}>Travel insurance</span>
                </Link>
        </div>
    )
}

export default Header