import { Component } from '@angular/core';
import { HeroComponent } from './components/hero/hero.component';
import { CategoriesComponent } from './components/categories/categories.component';
import { FeaturedProductsComponent } from './components/featured-products/featured-products.component';
import { BoosterBoxesComponent } from './components/booster-boxes/booster-boxes.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [HeroComponent, CategoriesComponent, FeaturedProductsComponent, BoosterBoxesComponent],
  templateUrl: './home.component.html',
})
export class HomeComponent {}
