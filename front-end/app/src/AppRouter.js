import React from 'react';
import {Switch, Route, Redirect} from 'react-router-dom';

import CarInsurance from './components/insurances/CarInsurance'
import HomeInsurance from './components/insurances/HomeInsurance'
import TravelInsurance from './components/insurances/TravelInsurance'

import SignInsurance from './components/SignInsurance'
import WrappedPayInsurance from './components/PayInsurance'
import CheckStatus from './components/CheckStatus'

import Home from './components/Home'

const AppRouter = (props) => {
    return (
    <Switch>

          <Route exact path='/' render={(props) =>
               <Home {...props}/>
                }
          />

          <Route exact path='/pay-insurance' render={(props) =>
               <WrappedPayInsurance {...props}/>
               }
          />

          <Route exact path='/check-status' render={(props) =>
              <CheckStatus  {...props}/>
              }
          />

          <Route exact path='/sign-insurance/car-insurance' render={(props) =>
              <CarInsurance  {...props}/>
               }
          />

          <Route exact path='/sign-insurance/home-insurance' render={(props) =>
               <HomeInsurance  {...props}/>
                }
          />

          <Route exact path='/sign-insurance/travel-insurance' render={(props) =>
                         <TravelInsurance   {...props}/>
                          }
                    />


          <Route exact path='/sign-insurance/car-insurance' render={(props) =>
              <CarInsurance {...props}/>
               }
          />

          <Route exact path='/sign-insurance' render={(props) =>
               <SignInsurance {...props}/>
               }
          />

          <Route exact path='/api/sign-insurance' render={(props) =>
                         <CheckStatus {...props}/>
                         }
                    />

    </Switch>
    )
}

export default AppRouter