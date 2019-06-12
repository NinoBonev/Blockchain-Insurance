import React from 'react';
import {Switch, Route, Redirect} from 'react-router-dom';

import CarInsurance from './components/insurances/CarInsurance'
import HomeInsurance from './components/insurances/HomeInsurance'
import TravelInsurance from './components/insurances/TravelInsurance'

import SignInsurance from './components/SignInsurance'
import PayInsurance from './components/PayInsurance'
import CheckStatus from './components/CheckStatus'

import Home from './components/Home'

const AppRouter = (props) => {
    return (
    <Switch>

          <Route exact path='/' render={(props) =>
               <Home />
                }
          />

          <Route exact path='/pay-insurance' render={(props) =>
               <PayInsurance />
               }
          />

          <Route exact path='/check-status' render={(props) =>
              <CheckStatus />
              }
          />

          <Route exact path='/sign-insurance/car-insurance' render={(props) =>
              <CarInsurance />
               }
          />

          <Route exact path='/sign-insurance/home-insurance' render={(props) =>
               <HomeInsurance />
                }
          />

          <Route exact path='/sign-insurance/travel-insurance' render={(props) =>
                         <TravelInsurance />
                          }
                    />


          <Route exact path='/sign-insurance/car-insurance' render={(props) =>
              <CarInsurance />
               }
          />

          <Route exact path='/sign-insurance' render={(props) =>
               <SignInsurance />
               }
          />

    </Switch>
    )
}

export default AppRouter