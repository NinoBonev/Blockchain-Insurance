import React from 'react'

import Header from './Header'
import Form from './Form'

import '../../style/app.css'


class CarInsurance extends React.Component {
    render() {
        return (
            <div className='car'>
                <div className='container-home-screen'>
                      <h1 style={{marginLeft: '21%'}}>Car insurance policy:</h1>
                      <Form policy='car' {...this.props} />
                      <Header />
                      <div style={{margin: '50px'}}>I would normally create a way to separate all the different kinds of insurances, so it's better
                                      organised for the user and the company as well. Would also make 2-3 plans for each type of insurance (Basic,
                                      Premium, Gold) each one of the with a constant amount for the policy. Also, when going to sign a new
                                      insurance to show all the current once you have for that category (if any) .... and so on with some
                                       more nice features for the user</div>

                </div>
            </div>
        )
    }
}

export default CarInsurance