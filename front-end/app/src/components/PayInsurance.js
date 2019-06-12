import React from 'react'
import {Link} from 'react-router-dom'

function PayInsurance() {
    return (
        <div className='one'>
            <div className='container-home-screen'>
                <div className='insurance-list'>
                <form action="/pay-insurance" method="POST">
                    <label className='insurance-list' for='id'>Pay with your ID</label>
                     <input name='id'  />
                     <input type="submit" class="btn btn-primary" value="Pay" />
                </form>
                </div>
            </div>
        </div>
    )
}

export default PayInsurance