import React from 'react';
import {Component} from 'react'
import {Layout} from 'antd'

import AppRouter from './AppRouter'
import Header from './components/Header'

function App() {
  return (
  <Layout>
     <Header />
        <Layout>
            <AppRouter />
        </Layout>
  </Layout>


  );
}

export default App;
