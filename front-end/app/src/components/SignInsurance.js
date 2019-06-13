import React from 'react'
import {Link} from 'react-router-dom'

import Header from './insurances/Header'


function SignInsurance() {
    return (
        <div className='one'>
            <div className='container-home-screen'>

                <h1 style={{marginTop: '20px', textAlign: 'center'}}>
                        Please select the type of insurance you would like.
                </h1>

                <div style={{margin: '50px'}}>I would normally create a way to separate all the different kinds of insurances, so it's better
                organised for the user and the company as well. Would also make 2-3 plans for each type of insurance (Basic,
                Premium, Gold) each one of the with a constant amount for the policy. Also, when going to sign a new
                insurance to show all the current once you have for that category (if any) .... and so on with some
                 more nice features for the user</div>

                 <Header />
            </div>
        </div>
    )
}

export default SignInsurance