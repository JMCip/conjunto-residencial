import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GestionFacturas } from './gestion-facturas';

describe('GestionFacturas', () => {
  let component: GestionFacturas;
  let fixture: ComponentFixture<GestionFacturas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionFacturas],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionFacturas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
