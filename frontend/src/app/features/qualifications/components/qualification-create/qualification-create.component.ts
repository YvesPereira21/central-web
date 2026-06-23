import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { QualificationService } from '../../services/qualification.service';
import { QualificationCreate } from '../../../../core/models/qualification';

@Component({
  selector: 'app-qualification-create',
  imports: [ReactiveFormsModule],
  templateUrl: './qualification-create.component.html',
  styleUrl: './qualification-create.component.css'
})
export class QualificationCreateComponent {
  private formBuilder = inject(FormBuilder);
  private qualificationService = inject(QualificationService);

  isSubmiting: boolean = false;
  qualificationForm = this.formBuilder.group({
    jobTitle: [''],
    experienceLevel: [''],
    institution: [''],
    startDate: [''],
    endDate: ['']
  });

  onSubmit() {
    if (this.qualificationForm.invalid) return alert('Preencha o formulário com informações corretas.');
    this.isSubmiting = true;

    const formValues = this.qualificationForm.value;
    const qualification: QualificationCreate = {
      jobTitle: formValues.jobTitle!,
      experienceLevel: formValues.experienceLevel!,
      institution: formValues.institution!,
      startDate: formValues.startDate!,
      endDate: formValues.endDate!,
    }

    this.qualificationService.createQualification(qualification).subscribe({
      next: (response) => {
        alert('Experiência registrada com sucesso!');
        console.log('Experiência registrada com sucesso!')
        this.clearForm();
      },
      error: (error) => {
        alert('Erro ao registrar experiência. Verifique os dados e tente novamente.');
        console.log('Erro ao registrar experiência.', error);
        this.clearForm();
      }
    })
  }

  clearForm() {
    this.qualificationForm.reset();
    this.isSubmiting = false;
  }
}
