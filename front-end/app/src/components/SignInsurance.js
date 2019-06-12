import React from 'react'
import {Link} from 'react-router-dom'

function SignInsurance() {
    return (
        <div className='one'>
            <div className='container-home-screen'>
                <ul className='insurance-list no-decoration'>
                    <Link to={{pathname: '/sign-insurance/home-insurance'}}><li>Home insurance</li></Link>
                    <Link to={{pathname: '/sign-insurance/car-insurance'}}><li>Car insurance</li></Link>
                    <Link to={{pathname: '/sign-insurance/travel-insurance'}}><li>Travel insurance</li></Link>
                </ul>
            </div>
        </div>
    )
}

export default SignInsurance