import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { QualificationService } from '../../../features/qualifications/services/qualification.service';
import { QualificationCreate } from '../../../features/models/qualification';

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
        console.log('Experiência registrada com sucesso!')
        this.clearForm();
      },
      error: (error) => {
        console.log('Erro ao registrar experiência.');
        this.clearForm();
      }
    })
  }

  clearForm() {
    this.qualificationForm.reset();
    this.isSubmiting = false;
  }
}
