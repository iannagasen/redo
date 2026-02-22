import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavBar } from './components/shared/navbar/navbar';

@Component( {
  selector: 'app-root',
  imports: [ RouterOutlet, NavBar ],
  templateUrl: './app.html',
  styleUrl: './app.css'
} )
export class App {
}
